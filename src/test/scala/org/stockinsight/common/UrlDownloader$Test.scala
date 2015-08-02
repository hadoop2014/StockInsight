package org.stockinsight.common

import java.io.File

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
 * Created by asus on 2015/7/2.
 */
class UrlDownloader$Test extends FlatSpec with Matchers with LogSupport {
  it should "download file from given url" in {
    //UrlDownloader.downloadFileJava(StockIndexID.getStockIndexUrl(idHangshengIndex),StockIndexID.getStockIndexFileName(idHangshengIndex))
    //downloadFileJava("http://www.sina.com.cn","HangShengIndex.csv")
    val file = new File(".")
    //file.listFiles().filter(_.isFile).filter(_.exists("ShangHaiIndex.csv")).foreach(println)
    val fileLength =Source.fromFile("ShangHaiIndex.csv").length
    fileLength should not be { 0}
  }

}
