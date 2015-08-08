package org.stockinsight.common

import org.scalatest.{Matchers, FlatSpec}
import org.stockinsight.Hdfs.HdfsManager

/**
 * Created by asus on 2015/8/2.
 */
class StockIndexID$Test extends FlatSpec with LogSupport with Matchers {

  val stockIndexId = StockIndexID.idDowJonesIndex

  it should "get pathname of hdfs from stockid" in{
    StockIndexID.getStockIndexPathName(stockIndexId) should be ("DowJonesIndustrialAverageIndex")
  }

  it should "data of local dir and data of hdfs dir should be right" in {
    val localPath = ConfigManager.dataPath
    val hdfsPath = HdfsManager.getDefaultFS() + ConfigManager.dataHdfsPath

    log.debug(s"${localPath}${StockIndexID.getStockIndexFileName(stockIndexId)}")
    log.debug(s"$hdfsPath${StockIndexID.getStockIndexPathName(stockIndexId)}/${StockIndexID.getStockIndexFileName(stockIndexId)}")
  }

}
