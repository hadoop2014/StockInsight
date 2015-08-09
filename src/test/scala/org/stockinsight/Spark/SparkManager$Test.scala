package org.stockinsight.Spark

import org.scalatest.{FlatSpec, Matchers}
import org.stockinsight.common.LogSupport

/**
 * Created by asus on 2015/8/8.
 */
class SparkManager$Test extends FlatSpec with  Matchers with LogSupport {

  it should "using hive context" in {
    val tables = SparkManager.showTables()
    tables.foreach(log.info)
    tables.filter(_.contains("index")).size should be (6)
  }

}
