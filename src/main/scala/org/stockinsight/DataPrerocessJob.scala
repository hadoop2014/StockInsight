package org.stockinsight

/**
 * Created by asus on 2015/8/5.
 */
object DataPrerocessJob extends App{

  val dataPreprocess = new DataPreprocess

  //获取指数数据
  dataPreprocess.downloadFileFromUrl

  //删除文件的第一行（即标题头）
  dataPreprocess.deleteHeadLine()

  //指数数据推送到HDFS
  dataPreprocess.copyStockIndexDataToHdfs()

  //在hdfs上创建外部表
  dataPreprocess.createTable

}
