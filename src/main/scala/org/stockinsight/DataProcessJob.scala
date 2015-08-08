package org.stockinsight

/**
 * Created by asus on 2015/8/5.
 */
object DataProcessJob extends App{
  val dataPreprocess = new DataPreprocess

  //获取指数数据
  dataPreprocess.downloadFileFromUrl

  //指数数据推送到HDFS
  dataPreprocess.copyStockIndexDataToHdfs()

}
