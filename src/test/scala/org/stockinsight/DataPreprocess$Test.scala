package org.stockinsight

import org.scalatest.{FlatSpec, Matchers}
import org.stockinsight.common.{ConfigManager, LogSupport, StockIndexID}

import scala.io.Source

/**
 * Created by asus on 2015/8/2.
 */
class DataPreprocess$Test extends FlatSpec with LogSupport with Matchers{

  val dataPreprocess = new DataPreprocess
  /*
  it should "get the url for download stockindex" in {

    dataPreprocess.downloadFileFromUrl
    val files = new File(ConfigManager.dataPath).listFiles()
    files.filter(_.isFile).filter(_.length() != 0).size should be (StockIndexID.values.size)
  }

  it should "put data to hdfs" in {

    dataPreprocess.copyStockIndexDataToHdfs()
    val files = HdfsManager.listFiles(s"${HdfsManager.getDefaultFS()}${ConfigManager.dataHdfsPath}")
    files.filter(_.contains("Index")).size should be (StockIndexID.values.size)
  }

  it should "create table by spark sql with hivecontext" in{
    dataPreprocess.createTable
    SparkManager.showTables().filter(_.contains("index")).size should be (6)
  }
  */

  it should "delete headline " in {
    dataPreprocess.deleteHeadLine()
    val stockIndexId = StockIndexID.idHushen300Index
    val filename = ConfigManager.dataPath + StockIndexID.getStockIndexFileName(stockIndexId)
    Source.fromFile(filename).getLines().toList.head.split(",")(0) should not be "Date"
  }

}
