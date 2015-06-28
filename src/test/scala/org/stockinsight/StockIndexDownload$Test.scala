package org.stockinsight

import java.util.Calendar
import org.scalatest.FlatSpec
import org.scalatest.ShouldMatchers
import org.stockinsight.common.{StockIndexID, LogSupport}
import org.stockinsight.common.Constant._

/**
 * Created by asus on 2015/6/28.
 */
class StockIndexDownload$Test extends FlatSpec with LogSupport with ShouldMatchers {
  it should "get the url for download stockindex" in{
    val result = StockIndexDownload.getStockIndexUrl("399001")
    result should be {"http://ichart.finance.yahoo.com/table.csv?s=%5EHSI &amp;d=5&amp;e=28&amp;f=2015&amp;g=d&amp;a=9&amp;b=1&amp;c=1928&amp;ignore=.csv"}
  }
}
