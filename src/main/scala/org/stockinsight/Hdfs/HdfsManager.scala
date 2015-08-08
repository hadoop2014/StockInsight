package org.stockinsight.Hdfs

import org.stockinsight.common._

/**
 * Created by asus on 2015/8/1.
 */
object HdfsManager extends HdfsFunction{

  val hdfsConf = ConfigManager.configHdfs()

  def getDfsNameServices(): String = {
    val dfsname = hdfsConf.get("dfs.nameservices")
    log.info(s"getDfsNameServices return $dfsname")
    dfsname
  }

  def getDefaultFS(): String = {
    val defaultFS: String = hdfsConf.get("fs.defaultFs")
    log.info(s"getDefaultFS return $defaultFS")
    defaultFS
  }

}


