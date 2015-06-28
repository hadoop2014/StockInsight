package org.stockinsight

import java.util.{Calendar, Date}

import org.stockinsight.common.StockIndexID.StockIndexID
import org.stockinsight.common._

/**
 * Created by asus on 2015/6/28.
 */
object StockIndexDownload {

  def getStockIndexUrl(idStockIndex:String):String = {
    val codeStockIndex =
    idStockIndex match {
      case idHangshengIndex => "HSI"
      case idDowJonesIndex => "DJI"
      case idNikkeiIndex => "N225"
    }
    val currentTime = Calendar.getInstance().getTime
    s"http://ichart.finance.yahoo.com/table.csv?s=%5E$codeStockIndex &amp;d=${currentTime.getMonth}&amp;e=${currentTime.getDate}&amp;f=${Calendar.getInstance().get(Calendar.YEAR)}&amp;g=d&amp;a=9&amp;b=1&amp;c=1928&amp;ignore=.csv"
  }
}
