package org.stockinsight.database

import org.stockinsight.Spark.SparkManager

/**
 * Created by asus on 2015/8/9.
 */

class Sql(private[this] val sqlStatements: String*) {
  val sqlStatement: String = sqlStatements.mkString(";")
  def query = SparkManager.query(sqlStatements)
}

object Sql {
  def apply(sqlStatements: String*) = new Sql(sqlStatements: _*)

  implicit def toSql(statement: String): Sql = new Sql(statement)
}
