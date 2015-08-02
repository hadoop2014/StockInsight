package org.stockinsight.Http

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.stockinsight.common.StockIndexID._
import org.stockinsight.common.{StockIndexID, LogSupport}
import java.io._

/**
 * Created by asus on 2015/8/2.
 */
class HttpManager$Test extends FlatSpec with LogSupport with ShouldMatchers{

  val stockIndexID = idDowJonesIndex

  it should "test download file from url." in {
    val url = StockIndexID.getStockIndexUrl(stockIndexID)
    val filename = StockIndexID.getStockIndexFileName(stockIndexID)
    HttpManager.downloadFromUrl(url,filename)
    val file = new File(filename)
    file.exists() should be (true)
    // val fileLength =Source.fromFile(filename).length
    file.length() should not be (0)
  }
}
