package org.stockinsight

import java.util.Calendar

import org.scalatest.{FlatSpec, Matchers}
import org.stockinsight.common.StockIndexID._
import org.stockinsight.common.{LogSupport, StockIndexID}


/**
 * Created by asus on 2015/6/28.
 */
class StockIndexDownload$Test extends FlatSpec with Matchers with LogSupport{
  it should "get the url for download stockindex" in {
    val result = StockIndexID.getStockIndexUrl(idDowJonesIndex)
    val currentTime = Calendar.getInstance().getTime
    result should be {
      s"http://ichart.finance.yahoo.com/table.csv?s=%5EDJI&amp;d=${currentTime.getMonth}&amp;e=${currentTime.getDate}&amp;f=${Calendar.getInstance().get(Calendar.YEAR)}&amp;g=d&amp;a=9&amp;b=1&amp;c=1928&amp;ignore=.csv"
    }
  }
}
