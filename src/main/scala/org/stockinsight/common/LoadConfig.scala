package org.stockinsight.common

import java.io.File
import scala.collection.JavaConversions._
import com.typesafe.config.{Config, ConfigFactory}

/**
 * Created by asus on 2015/8/8.
 */
trait LoadConfig extends LogSupport{

  var config: Config = null

  def getString(key: String): String =config.getString(key)

  def getStringList(key: String):List[String] = config.getStringList(key).toList

  /**
   * 设置配置文件，只写文件名即可。
   *
   * 默认从configHome路径去读文件，同时如果resources下有同名文件也会读取   *
   * 注意：在前面的文件会覆盖后面的文件的相同key的配置值
   *
   * @param files
   */
  def setConfigFiles(configHome:String,files: String*): Unit = {
    log.debug(s"config home: $configHome")
    config = files.toList.map(load(configHome,_)).reduce((a, b) => a.withFallback(b))
  }

  protected def load(configHome: String,file: String): Config = {
    val resourceFile = file
    val configFile = new File(makePath(configHome,file))
    if (configFile.exists()) {
      log.debug(s"loading file[${configFile.getPath}] and resource[$resourceFile]")
      ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load(resourceFile))
    }
    else {
      log.debug(s"loading resource[$resourceFile]")
      ConfigFactory.load(resourceFile)
    }
  }

  protected def makePath(configHome:String,fileName: String): String = {
    val newDir = configHome.trim.replaceAll( """\\""", "/")
    if (newDir.endsWith("/")) configHome + fileName
    else configHome + "/" + fileName
  }


}
