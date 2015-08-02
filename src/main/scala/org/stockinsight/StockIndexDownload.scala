package org.stockinsight

import org.stockinsight.common._

/**
 * Created by asus on 2015/6/28.
 */
object StockIndexDownload extends LogSupport{
  def downloadFileFromUrl = {
    try{
      StockIndexID.values.map(x => UrlDownloader.downloadFileJava(StockIndexID.getStockIndexUrl(x),StockIndexID.getStockIndexFileName(x)))
    }
    catch{
      case e: Throwable => {
        log.debug("downloadFileFromUrl error!")
        e.printStackTrace()
      }
    }
  }
}
