package org.stockinsight.common

import org.scalatest.{Matchers, FlatSpec}
import org.stockinsight.Hdfs.HdfsManager

/**
 * Created by asus on 2015/8/2.
 */
class ConfigManager$Test extends FlatSpec with LogSupport with Matchers {

  it should "the data of local dir and data of hdfs dir should be right" in {

    log.info(ConfigManager.dataPath)
    log.info(ConfigManager.dataHdfsPath)
    log.info(ConfigManager.projectName)
    log.info(ConfigManager.logHdfsPath)
    log.info(ConfigManager.artifactsPath)
    log.info(ConfigManager.targetJar)
    log.info(ConfigManager.masterUrl)
    log.info(ConfigManager.driverHost)
    ConfigManager.siteConfFiles.foreach(log.debug)
    ConfigManager.dataPath should be ("D:\\BigdataResearch\\StockInsight\\data\\")
    HdfsManager.getDefaultFS() + ConfigManager.dataHdfsPath should be ("hdfs://Master:9000/stockindex/")
    ConfigManager.masterUrl should be ("spark://Master:7077")
  }

}
