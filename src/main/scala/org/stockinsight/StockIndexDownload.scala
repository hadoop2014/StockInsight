package org.stockinsight

import org.stockinsight.common._

/**
 * Created by asus on 2015/6/28.
 */
object StockIndexDownload {


  def getStockIndexUrl(idStockIndex:String) = {
    "http://ichart.finance.yahoo.com/table.csv?s=%5E" & strIndexCode & "&amp;d=" & (DatePart("m", Date) - 1) & "&amp;e=" & DatePart("d", Date) & "&amp;f=" & Year(Date) & "&amp;g=d&amp;a=9&amp;b=1&amp;c=1928&amp;ignore=.csv"
  }


}
