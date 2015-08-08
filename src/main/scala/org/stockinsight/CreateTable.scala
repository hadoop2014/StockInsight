package org.stockinsight

import org.stockinsight.Hdfs.HdfsManager
import org.stockinsight.Spark.SparkManager
import org.stockinsight.common.{ConfigManager, StockIndexID}

/**
 * Created by asus on 2015/8/5.
 */

object CreateTable extends SparkManager {

  usingHiveContext("CreateTable"){
    hiveContext => {
      val dataHdfsPath = HdfsManager.getDefaultFS() + ConfigManager.dataHdfsPath

      StockIndexID.values.foreach{
        stockIndexId => {
          val tableName = StockIndexID.getStockIndexPathName(stockIndexId)
          val location = dataHdfsPath + tableName
          hiveContext.sql(s"""
          CREATE EXTERNAL TABLE IF NOT EXISTS $tableName (
            ReportDate STRING,
            OpenPrice DOUBLE,
            HighPrice DOUBLE,
            LowPrice DOUBLE,
            ClosePrice DOUBLE,
            Volume DOUBLE,
            AdjClosePrice DOUBLE
          )
          ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
          LOCATION '$location'
          """
          )
          log.info(s"Success to create table $tableName")
        }
      }
    }
  }
}
