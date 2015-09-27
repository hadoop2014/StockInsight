package org.stockinsight.common

import java.io.File

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

/**
 * Created by asus on 2015/8/2.
 */
object ConfigManager extends LogSupport with LoadConfig{

  val currentPath = System.getProperty("user.dir")

  val fileSeperate = System.getProperty("file.separator")   //文件路径分割符

  val configHome = ".\\src\\main\\resources\\"
  //读取所有配置文件
  setConfigFiles(configHome,"default.conf")

  val dataPath = currentPath + "\\data\\"    //存放数据的本地地址

  val projectName = currentPath.split("\\\\").last

  val dataHdfsPath = "/stockindex/"  //存放数据的HDFS地址

  val artifactsPath = s"$currentPath\\out\\artifacts\\${projectName}_jar"

  val targetJar = s"$artifactsPath\\${projectName}.jar"

  val sparkHost = getString("spark.host")  //从default.conf文件中获取配置
  val masterUrl = if(sparkHost.toLowerCase() == "localhost") "local[*]" else s"spark://$sparkHost:7077"
  val driverHost = getString("driver.host")
  val sparkParameters = getStringList("sparkContext.parameters")

  val siteConfFiles = List(s"${configHome}core-site.xml", s"${configHome}hdfs-site.xml", s"${configHome}hive-site.xml")

  //配置hdfsConfig
  def configHdfs(): Configuration = {
    val hdfsConfig = new Configuration()

    siteConfFiles.foreach{
      file =>
        if(new File(file).exists()){
          hdfsConfig.addResource(new Path(file))
          log.info(s"Hdfs manager loader file: $file in path:$currentPath")
        }
        else {
          val configPath = file.split("/").init.mkString("/")    //获取路径名
          val configFile = file.split("/").last

          if (new File(configFile).exists()){
            hdfsConfig.addResource(new Path(configFile))
            log.info(s"Hdfs manager loader file: $configFile in path:$currentPath,but not in path $configPath")
          }
          else {
            log.info(s"Hdfs manager loader file: $configFile failure, it is not in path:$currentPath or path $configPath")
          }
        }
    }
    hdfsConfig
  }

  val logHdfsPath = s"${configHdfs().get("fs.defaultFs")}/tmp/spark-events"

}
