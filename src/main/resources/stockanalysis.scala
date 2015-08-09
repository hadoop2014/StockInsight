import org.apache.spark.SparkContext
import org.kohsuke.args4j.Option
import oracle.recruitment.util.{HdfsUtils, Options}

import org.apache.spark.rdd.RDD
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import org.apache.spark.SparkContext._
import scala.io.Source
import Array._

class MyOptions (args: Array[String]) extends Options {
  @Option(name = "-sm", usage = "Cluster URL to connect to (e.g. mesos://host:port, spark://host:port, local[4]).")
  var sparkMaster: String = "local"

  @Option(name = "-sn", usage = "Application name, to display on the cluster web UI.")
  var sparkApplicationName: String = "StockAnalysis"

  @Option(name = "-sh", usage = "Spark home: Location where Spark is installed on cluster nodes.")
  var sparkHome: String = _

  var sparkJars: Seq[String] = _
  @Option(name = "-sj", usage = "Values are bar|delimited.  Collection of JARs to send to the cluster. " +
    "These can be paths on the local file system or HDFS, HTTP, HTTPS, or FTP URLs.")
  def setSparkJars(sparkJars: String) = this.sparkJars = sparkJars.split("\\|")

  @Option(name = "-i", usage = "HDFS url of input data." )
  var inputPath: String = _

  @Option(name = "-o", usage = "HDFS url of output data directory." )
  var outputPath: String = _

  def getSparkContext: SparkContext = {
    new SparkContext(sparkMaster, sparkApplicationName, sparkHome, sparkJars)
  }

  initialize ( args )
}



object StockAnalysis {

  //Below three functions are required for combineByKey function to convert tuples from
  // (price, volume, count) format to ((min-price, max-price, sum-price),
  //									(min-volume, max-volume, sum-volume), count) format
  def createCombiner(priceVolCnt:(Double, Long, Int)) :
  ((Double, Double, Double),(Long, Long, Long),Int) = {
    ((priceVolCnt._1, priceVolCnt._1, priceVolCnt._1),(priceVolCnt._2,priceVolCnt._2,priceVolCnt._2),priceVolCnt._3)
  }

  def mergeValue(combVal:((Double, Double, Double),(Long, Long, Long),Int),
                 singleVal:(Double, Long, Int)) :
  ((Double, Double, Double),(Long, Long, Long),Int) = {
    ((Math.min(singleVal._1,combVal._1._1), Math.max(singleVal._1,combVal._1._2),singleVal._1 + combVal._1._3),
      (Math.min(singleVal._2,combVal._2._1), Math.max(singleVal._2,combVal._2._2),singleVal._2 + combVal._2._3),
      combVal._3+singleVal._3)
  }

  def mergeCombiners(combVal1 : ((Double, Double, Double),(Long, Long, Long),Int),
                     combVal2 : ((Double, Double, Double),(Long, Long, Long),Int)) :
  ((Double, Double, Double),(Long, Long, Long),Int) = {
    ((Math.min(combVal1._1._1,combVal2._1._1), Math.max(combVal1._1._2,combVal2._1._2),combVal1._1._3 + combVal2._1._3),
      (Math.min(combVal1._2._1,combVal2._2._1), Math.max(combVal1._2._2,combVal2._2._2),combVal1._2._3 + combVal2._2._3),
      combVal1._3+combVal2._3)
  }

  //Compute Median and IQR on given RDD.
  // Input - (Stock symbol, Metric) tuples. The metric can be price or volume
  // Output - (Stock symbol, (Median, IQR)) tuples for each stock symbol.
  def computeMedianIQR(kvMap: RDD[(String, Double)]): RDD[(String, (Double, Double))] = {
    kvMap.groupByKey().mapValues(valLst => {
      val sortedList = valLst.sorted;
      (sortedList(valLst.size/2), sortedList(valLst.size*3/4)-sortedList(valLst.size/4))
    })
  }

  //Compute Mode on given RDD.
  // Input - (Stock symbol, Metric) tuples. The metric can be price or volume
  // Output - (Stock symbol, (Mode, Mode count)) tuples for each stock symbol.
  def computeMode(kvMap: RDD[(String, Double)]): RDD[(String, (Double,Int))] = {
    /*
     * Group by stock symbol and each value of given metric, reduce adding the counts for each
     * such combination. Remap with stock symbol as the key and (metric value, count) as the value.
     * Reduce this map by retaining the metric value with largest count for each stock symbol
     */
    kvMap.map(kv => ((kv._1,kv._2),1)).reduceByKey(_+_).map(kv => (kv._1._1, (kv._1._2,kv._2))).reduceByKey(
      (a,b) => if (a._2 > b._2) a else b)}

  //Compute frequency distribution of given metric
  //Input - (Stock symbol, (Sequence of metric values, (min. value, max. value))) tuples
  //Output - (Stock symbol, (bucket id, frequency)) tuples for each stock symbol
  def computeFrequencies(kvMap: RDD[(String, (Seq[Double],(Double, Double)))]): RDD[(String, Seq[(Int, Long)])] = {
    /*
     * compute the bucket size based on min. and max. values for each stock symbol
     * Map the metric values into appropriate buckets and reduce the resulting map counting
     * the no. of entries in each bucket. Finally, group the entries based on stock symbol.
     */
    kvMap.flatMap(s_vlist_minmax => {
      val min = s_vlist_minmax._2._2._1;
      val max = s_vlist_minmax._2._2._2;
      val size = (max-min)/20;
      s_vlist_minmax._2._1.map(elem =>
        ((s_vlist_minmax._1, if (elem==min) 1 else Math.ceil((elem-min)/size).toInt),1)
      )}).reduceByKey(_+_).map(sb_fr => (sb_fr._1._1,(sb_fr._1._2,sb_fr._2.toLong))).groupByKey()
  }

  /*
   * compute Pearson correlation coefficient for pairs of stock symbols on given data
   * Input - (Date, (Stock Symbol, Metric for which PCC has to be computed)) tuples
   * 			(Stock symbol, mean of the metric for which PCC has to be computed) tuples
   *    		(Stock symbol, standard deviation of the metric for which PCC has to be computed) tuples
   * Output - (Stock symbol pair, PCC of the pair) tuples
   */
  def computePCC(kvMap: RDD[(String, (String, Double))],
                 meanMap: RDD[(String, Double)],
                 sdMap : RDD[(String, Double)]) : RDD[((String, String), Double)] = {
    //join given (stock symbol, metric) on date to generate pairs of (stock symbol, metric) for each date. Then filter the pairs to remove duplicate pairs
    //(symb1, symb2) and (symb2, symb1). Remap the filtered result to generate tuples of the form ((symb1, symb2),(metric pair))
    val pairsMap = kvMap.join(kvMap).filter(v => (v._2._1._1 < v._2._2._1)).map(v => ((v._2._1._1, v._2._2._1), (v._2._1._2, v._2._2._2)))

    //generate mean pairs as ((symb1, symb2), mean pair) tuples
    val meanPairsMap = meanMap.cartesian(meanMap).map(v => ((v._1._1, v._2._1), (v._1._2, v._2._2)))

    //generate sd pairs as ((symb1, symb2), mean pair) tuples
    val sdPairsMap = sdMap.cartesian(sdMap).map(v => ((v._1._1, v._2._1), (v._1._2, v._2._2)))

    //join the above RDDs and execute PCC formula on all pairs of the given metric for each stock symbol pair
    val meanSDPairsMap = meanPairsMap.join(sdPairsMap)
    pairsMap.groupByKey().join(meanSDPairsMap).map(v => {
      // v <- (sym pair,([price pairs],((mean1, mean2),(sd1, sd2))))
      var mean1 = v._2._2._1._1;
      var mean2 = v._2._2._2._2;
      var sd1 = v._2._2._2._1;
      var sd2 = v._2._2._2._2;
      var count = v._2._1.size();
      (v._1, v._2._1.map(prPair => {
        (prPair._1 - mean1) * (prPair._2 - mean2)
      }).reduce(_ + _) / (sd1 * sd2 * count))
    })
  }

  /*
   * Compute Kurtosis for each stock symbol for the given metric (price or volume)
   * Input - SparkContext, (Stock Symbol, Metric) tuples, (Stock Symbol, Mean) tuples,
   * 				(Stock Symbol, SD) tuples,(Stock Symbol, Count) tuples
   * Output - (Stock Symbol, Kurtosis) tuples
   */
  def computeKurtosis(sc : SparkContext,
                      kvMap : RDD[(String, Double)],
                      meanMap : RDD[(String, Double)],
                      sdMap : RDD[(String, Double)],
                      cntMap : RDD[(String, Int)]) : RDD[(String, Double)] = {

    //Broadcast the mean, standard deviation and count data since these are common in
    //all the nodes in the cluster
    val meanBC = sc.broadcast(meanMap.collectAsMap)
    val sdBC = sc.broadcast(sdMap.collectAsMap)
    val cntBC = sc.broadcast(cntMap.collectAsMap)

    //Map the entries in the given kvMap to compute the kurtosis for each stock symbol
    kvMap.map(x => (x._1, Math.pow((x._2 - meanBC.value(x._1))/sdBC.value(x._1),4)))
      .reduceByKey(_+_).map( y => {
      val n = cntBC.value(y._1)
      (y._1, ((n*(n+1)*(y._2))/((n-1)*(n-2)*(n-3)))-((3.0*(n-1)*(n-1))/((n-2)*(n-3))))
    } )
  }

  /*
   * Compute the moving average for given RDD
   * Input - (Stock symbol, (Date, metric such as price or volume)) tuples
   * Output - (Stock symbol, (Date, 3-day moving average ending on given date)) tuples
   */
  def computeMovingAvg(stkDateDataMap : RDD[(String,(String, Double))]) : RDD[(String,(String, Double))]= {
    //group the tuples by stock symbol, sort the values by date and compute the 3-day moving
    //average for every 3 entries
    stkDateDataMap.groupByKey().flatMapValues(x => {
      var mavg = List[(String, Double)]()
      var sum = 0.0
      var cnt = 0
      /*
       * Design note - sorting the values like below will not scale in "big data" scenarios
       * Alternate ways using external sort must be checked
       */
      x.sorted.foreach(s => {
        sum += s._2
        cnt += 1
        if (cnt==3) {
          mavg = mavg:::List[(String, Double)]((s._1, sum/3))
          cnt = 0
          sum=0.0
        }
      })
      mavg
    })
  }


  /*
   * Design note -
   * The collectAsMap() used in the below functions will collect the data into master node. This can
   * be a concern on large datasets, but is being used now to facilitate creating a single
   * file that can be readily read for visualization. Dumping the RDD directly creates multiple
   * files. An alternative has to be explored.
   */

  //Form a string representation of count, min.price, max. price, mean price, min. volume
  // max. volume and mean volume for each stock symbol.
  def getPrVolMinMaxMeanStr(kvMap: RDD[(String, (Double, Double, Double, Long, Long, Double, Int))] ) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tCount\tMin. Price\tMax. Price\tMean Price\tMin. Volume\tMax. Volume\tMean Volume\n")
    kvMap.collectAsMap().foreach(kv => {sb.append(f"${kv._1}%4s\t${kv._2._7}%5d\t${kv._2._1}%10.2f\t${kv._2._2}%10.2f\t${kv._2._3}%10.2f\t${kv._2._4}%11d\t${kv._2._5}%11d\t${kv._2._6}%11.2f\n")})
    sb.toString
  }

  //Form a string representation of median and IQR for closing price of each stock symbol
  def getPrMedianIQRStr(kvMap :  RDD[(String,(Double, Double))]) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tMedian Price\tIQR(Price)\n")
    kvMap.collectAsMap().foreach(kv => {sb.append(f"${kv._1}%4s\t${kv._2._1}%12.2f\t${kv._2._2}%10.2f\n")})
    sb.toString
  }

  //Form a string representation of mode of closing price of each stock symbol
  def getPrModeStr(kvMap :  RDD[(String, (Double, Int))]) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tMode Price\n")
    kvMap.collectAsMap().foreach(kv => {sb.append(f"${kv._1}%4s\t${kv._2._1}%10.2f\n")})
    sb.toString
  }

  //Form a string representation of median and IQR for volume of each stock symbol
  def getVolMedianIQRStr(kvMap:  RDD[(String, (Long, Long))]) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tMedian Volume\tIQR(Volume)\n")
    kvMap.collectAsMap().foreach(kv => {sb.append(f"${kv._1}%4s\t${kv._2._1}%13d\t${kv._2._2}%11d\n")})
    sb.toString
  }

  //Form a string representation of mode of volume of each stock symbol
  def getVolModeStr(kvMap: RDD[(String, (Long, Int))]) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tMode Volume\n")
    kvMap.collectAsMap().foreach(kv => {sb.append(f"${kv._1}%4s\t${kv._2._1}%11d\n")})
    sb.toString
  }

  //Form a string representation of variance and standard deviation of closing price and volume of each stock symbol
  def getPrVolVarSDStr(kvMap :  RDD[(String,((Double, Double),(Double, Double)))]) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tVariance(Price)\tSD(Price)\tVariance(Volume)\tSD(Volume)\n")
    kvMap.collectAsMap().foreach(kv => {sb.append(f"${kv._1}%4s\t${kv._2._1._1}%15.2f\t${kv._2._1._2}%9.2f\t${kv._2._2._1}%16.2f\t${kv._2._2._2}%10.2f\n")})
    sb.toString
  }

  //Form a string representation of frequency distribution of given metric of each stock symbol
  def getHistStr(kvMap : RDD[(String, Seq[(Int, Long)])], bktName:String) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\t$bktName\tFrequency\t\n")
    kvMap.collectAsMap().foreach(kv => {kv._2.map(bkt_freq => {sb.append(f"${kv._1}%4s\t${bkt_freq._1}%13d\t${bkt_freq._2}%9d\n")})})
    sb.toString
  }

  //Form a string representing PCC data in the given RDD in the form of a matrix
  def getPCCStr(kvMap : RDD[((String,String), Double)]) : String = {
    var sb = new StringBuilder
    var symbolList = ListBuffer[String]()
    var invSymMap = new HashMap[Int, String]
    var symIdx = 0
    var collMap = kvMap.collectAsMap
    collMap.keys.foreach( v => {
      if (!symbolList.contains(v._1) ) symbolList += v._1
      if (!symbolList.contains(v._2) ) symbolList += v._2
    })
    val sortedLst = symbolList.sorted
    sb.append(f"     \t")
    sortedLst.foreach(sym => {
      invSymMap+=symIdx->sym
      symIdx+=1
      sb.append(f"$sym%5s\t")
    })
    sb.append("\n")
    for (i <- 0 to symIdx-1) {
      var symb1 = invSymMap(i)
      sb.append(f"$symb1%5s\t")
      for (j <- 0 to i)
        sb.append(f"     \t")
      for (j <- i+1 to symIdx-1) {
        var symb2 = invSymMap(j)
        var pcc = collMap(symb1,symb2)
        sb.append(f"$pcc%.3f\t")
      }
      sb.append("\n")
    }
    sb.toString
  }

  //Form the string representation of moving averages in the given map
  def getMovingAvgStr(mavgMap : RDD[(String, (String, Double))]) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tDate      \tAverage\n")
    mavgMap.collect().foreach(s => sb.append(f"${s._1}%4s\t${s._2._1}%10s\t${s._2._2}%.2f\n"))
    sb.toString
  }

  //Form the string representation of kurtosis of stock symbols in given map
  def getKurtosisStr(kurtMap : RDD[(String, Double)]) : String = {
    var sb = new StringBuilder
    sb.append(f"Sym\tKurtosis\t\n")
    kurtMap.collect().foreach(x => sb.append(f"${x._1}%4s\t${x._2}%.2f\n"))
    sb.toString
  }

  def main ( args: Array[String] ) {

    // options
    val options: MyOptions = new MyOptions ( args )

    // spark context
    val spark: SparkContext = options.getSparkContext

    var hdrMap = new HashMap[String, Int]
    val SYM = "Sym"
    val DATE = "Date"
    val OPEN_PR = "Open"
    val HIGH_PR = "High"
    val LOW_PR = "Low"
    val CLOSE_PR = "Close"
    val VOLUME = "Volume"
    val ADJUSTED = "Adjusted"

    //Parse the input file to create an RDD to be used in subsequent computations
    val inpFileMap = spark.textFile(options.inputPath).map(ln => {
      val toks = ln.split(',');
      if (toks(0).equals(SYM)) {
        var i = 0
        toks.foreach(fl => {hdrMap += fl -> i;i=i+1})
      }
      toks
    }).filter(toks => !toks(0).equals("Sym"))

    // RDD of closing price and volume for all entries in the input
    val prVolMap = inpFileMap.map(toks => (toks(hdrMap(SYM)), (toks(hdrMap(CLOSE_PR)).toDouble, toks(hdrMap(VOLUME)).toLong,1)))

    // RDD of closing price for all entries in the input
    val prMap = inpFileMap.map(toks => (toks(hdrMap(SYM)),toks(hdrMap(CLOSE_PR)).toDouble))

    // RDD of volume for all entries in the input
    val volMapD = inpFileMap.map(toks => (toks(hdrMap(SYM)),toks(hdrMap(VOLUME)).toDouble))

    //RDD of min(price), max(price), mean(price), min(volume), max(volume), mean(volume), count
    val prVolMinMaxMeanMap = prVolMap.combineByKey(createCombiner, mergeValue, mergeCombiners).mapValues(v => (v._1._1, v._1._2, v._1._3/v._3, v._2._1, v._2._2, v._2._3.toDouble/v._3, v._3))

    //RDD of median and IQR of closing price
    val prMedianIQRMap = computeMedianIQR(prMap)

    //RDD of mode of closing price
    val prModeMap = computeMode(prMap)

    //RDD of median and IQR of volume
    val volMedianIQRMap = computeMedianIQR(volMapD).mapValues(v => (v._1.toLong, v._2.toLong))

    //RDD of mode of volume
    val volModeMap = computeMode(volMapD).mapValues(v =>(v._1.toLong,v._2) )

    //RDD of ((price_variance, price_sd),(volume_variance, volume_sd))
    val prVolVarSDMap = inpFileMap.map(toks => (toks(hdrMap(SYM)), (toks(hdrMap(CLOSE_PR)).toDouble, toks(hdrMap(VOLUME)).toLong))).groupByKey().join(
      prVolMinMaxMeanMap.mapValues(v=> (v._3,v._6))).mapValues(pvlist_means => {
      var var1 = pvlist_means._1.map(pv => ((pv._1-pvlist_means._2._1)*(pv._1-pvlist_means._2._1),
        ((pv._2-pvlist_means._2._2)*(pv._2-pvlist_means._2._2)))).reduce((a,b)=>
        (a._1+b._1, a._2+b._2));
      var pv_variance = (var1._1/pvlist_means._1.size-1, var1._2/pvlist_means._1.size-1);
      ((pv_variance._1, Math.sqrt(pv_variance._1)),(pv_variance._2, Math.sqrt(pv_variance._2)))
    })

    //RDD of frequency distribution of price
    val prFreqMap = computeFrequencies(prMap.groupByKey().join(prVolMinMaxMeanMap.mapValues(v=>(v._1, v._2))))

    //RDD of frequency distribution of volume
    val volFreqMap = computeFrequencies(volMapD.groupByKey().join(prVolMinMaxMeanMap.mapValues(v=>(v._4, v._5))))

    //RDD of Pearson correlation coefficient of closing price
    val pricePCC = computePCC(inpFileMap.map(toks => (toks(hdrMap(DATE)), (toks(hdrMap(SYM)), toks(hdrMap(CLOSE_PR)).toDouble))), // date <- (symbol, price)
      prVolMinMaxMeanMap.mapValues(_._3), prVolVarSDMap.mapValues(_._1._2))

    //RDD of Pearson correlation coefficient of volume
    val volumePCC = computePCC(inpFileMap.map(toks => (toks(hdrMap(DATE)), (toks(hdrMap(SYM)), toks(hdrMap(VOLUME)).toDouble))), // date <- (symbol, volume)
      prVolMinMaxMeanMap.mapValues(_._6), prVolVarSDMap.mapValues(_._2._2))

    //compute kurtosis of closing prices
    val priceKurt = computeKurtosis(spark, prMap, prVolMinMaxMeanMap.mapValues(_._3), prVolVarSDMap.mapValues(_._1._2), prVolMinMaxMeanMap.mapValues(_._7))

    //compute kurtosis of volume
    val volumeKurt = computeKurtosis(spark, volMapD, prVolMinMaxMeanMap.mapValues(_._6), prVolVarSDMap.mapValues(_._2._2), prVolMinMaxMeanMap.mapValues(_._7))

    //compute 3-day moving average of closing price of each stock symbol
    val priceMavg = computeMovingAvg(inpFileMap.map(toks=>(toks(hdrMap(SYM)),(toks(hdrMap(DATE)),toks(hdrMap(CLOSE_PR)).toDouble))))

    //compute 3-day moving average of volume of each stock symbol
    val volumeMavg = computeMovingAvg(inpFileMap.map(toks=>(toks(hdrMap(SYM)),(toks(hdrMap(DATE)),toks(hdrMap(VOLUME)).toDouble))))

    //print the above computed RDD in tab delimited format into HDFS
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "count_min_max_mean.txt", spark.hadoopConfiguration, getPrVolMinMaxMeanStr(prVolMinMaxMeanMap), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "price_median_IQR.txt", spark.hadoopConfiguration, getPrMedianIQRStr(prMedianIQRMap), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "price_mode.txt", spark.hadoopConfiguration, getPrModeStr(prModeMap), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "volume_median_IQR.txt", spark.hadoopConfiguration, getVolMedianIQRStr(volMedianIQRMap), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "volume_mode.txt", spark.hadoopConfiguration, getVolModeStr(volModeMap), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "variance_sd.txt", spark.hadoopConfiguration, getPrVolVarSDStr(prVolVarSDMap), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "priceHistogram.txt", spark.hadoopConfiguration, getHistStr(prFreqMap, "Price Bucket"), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "volHistogram.txt", spark.hadoopConfiguration, getHistStr(volFreqMap, "Volume Bucket"), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "pricePCC.txt", spark.hadoopConfiguration, getPCCStr(pricePCC), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "volumePCC.txt", spark.hadoopConfiguration, getPCCStr(volumePCC), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "priceKurt.txt", spark.hadoopConfiguration, getKurtosisStr(priceKurt), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "volumeKurt.txt", spark.hadoopConfiguration, getKurtosisStr(volumeKurt), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "priceMovingAvg.txt", spark.hadoopConfiguration, getMovingAvgStr(priceMavg), true )
    HdfsUtils.putHdfsFileText(options.outputPath + "/" + "volumeMovingAvg.txt", spark.hadoopConfiguration, getMovingAvgStr(volumeMavg), true )

    // stop Spark
    spark.stop()
  }

}