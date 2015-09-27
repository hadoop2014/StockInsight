package org.stockinsight.Spark

import java.io.IOException

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}
import org.stockinsight.common.{ConfigManager, LogSupport}

/**
 * Created by asus on 2015/8/9.
 */
trait SparkFunction extends LogSupport{
  //实现SparkContext的租借模式
  def usingSparkContext[T](appName: String)(f: SparkContext => T): T = {

    val sparkConf = new SparkConf().setAppName(appName)
    sparkConf.setMaster(ConfigManager.masterUrl).set("spark.driver.host",s"${ConfigManager.driverHost}")
    ConfigManager.sparkParameters.foreach(x => {
      val parameter = x.split(",")
      sparkConf.set(parameter.head, parameter.last)
    })
    sparkConf.set("spark.eventLog.dir",s"${ConfigManager.logHdfsPath}")
    sparkConf.setJars(List(s"${ConfigManager.targetJar}"))

    val sparkContext = new SparkContext(sparkConf)

    try {
      f(sparkContext)
    }
    catch {
      case e:IOException =>
        log.error(s"$appName IO error in usingSparkContext" ,e)
        throw e
      case ex: Throwable =>
        log.error(s"$appName error in usingSparkContext" , ex)
        throw ex
    }
    finally{
      try{
        if(sparkContext != null) sparkContext.stop()
      }catch {
        case e:Throwable =>
          log.error(appName + " stop spark context failed.",e)
          e.printStackTrace()
      }
    }
  }

  //实现HiveContext的租借模式
  def usingHiveContext[T](appName: String)(f: HiveContext => T): T = {
    usingSparkContext(appName){
      sparkContext => {
        val hiveContext = new HiveContext(sparkContext)
        try{
          f(hiveContext)
        }
        catch {
          case e: Throwable =>
            log.error(s"$appName error in usingHiveContext.",e)
            throw e
        }
      }
    }
  }
}
