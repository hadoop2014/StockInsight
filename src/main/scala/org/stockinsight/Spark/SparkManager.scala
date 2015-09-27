package org.stockinsight.Spark

//import java.util.{List => JList}

/**
 * Created by asus on 2015/8/8.
 */
object SparkManager extends SparkFunction{


  def showTables(): Array[String]= {
    usingHiveContext("show tables"){
      hiveContext => {
        hiveContext.sql("show tables").collect().map(_.getString(0))
      }
    }
  }

  def query(sqlStatement: Seq[String]) ={
    usingHiveContext("sql query"){
      hiveContext => {
        sqlStatement.init.map(hiveContext.sql(_))
        hiveContext.sql(sqlStatement.last).collect()
      }
    }
  }
}
