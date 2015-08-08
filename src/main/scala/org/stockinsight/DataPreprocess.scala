package org.stockinsight

import org.stockinsight.Hdfs.HdfsManager
import org.stockinsight.Http.HttpManager
import org.stockinsight.common._

/**
 * Created by asus on 2015/6/28.
 */
//数据预处理，包括从internet上获取指数数据，同时存入hdfs
class DataPreprocess extends LogSupport{

  def downloadFileFromUrl(): Unit = {
    try{
      StockIndexID.values.map(
        stockIndexID => {
          HttpManager.downloadFromUrl(StockIndexID.getStockIndexUrl(stockIndexID), ConfigManager.dataPath + StockIndexID.getStockIndexFileName(stockIndexID))
          log.info(s"Success to get ${StockIndexID.getStockIndexFileName(stockIndexID)} from internet!")
        }
      )
    }
    catch{
      case e: Throwable => {
        log.error("downloadFileFromUrl error!",e)
        e.printStackTrace()
      }
    }
  }

  //拷贝指数数据到HDFS
  def copyStockIndexDataToHdfs(): Unit = {
    try{
      val localPath = ConfigManager.dataPath
      val hdfsPath = HdfsManager.getDefaultFS() + ConfigManager.dataHdfsPath
      StockIndexID.values.map(
        stockIndexID => {
          HdfsManager.copyFromLocalFile(s"${localPath}${StockIndexID.getStockIndexFileName(stockIndexID)}",
            s"$hdfsPath${StockIndexID.getStockIndexPathName(stockIndexID)}/${StockIndexID.getStockIndexFileName(stockIndexID)}")
          log.info(s"Success to put ${localPath}${StockIndexID.getStockIndexFileName(stockIndexID)} to HDFS ($hdfsPath${StockIndexID.getStockIndexPathName(stockIndexID)}/)!")
        }
      )
    }
    catch{
      case e: Throwable => {
        log.error("copy data to hdfs error!",e)
        e.printStackTrace()
      }
    }

  }
}
