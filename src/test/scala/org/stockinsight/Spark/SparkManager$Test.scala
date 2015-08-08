package org.stockinsight.Spark

import org.scalatest.{FlatSpec, Matchers}
import org.stockinsight.common.LogSupport

/**
 * Created by asus on 2015/8/8.
 */
class SparkManager$Test extends FlatSpec with LogSupport with  Matchers with SparkManager {

  it should "using hive context" in {

    usingHiveContext("show tables"){
      hiveContext => {
        val tables = hiveContext.sql("show tables")
        tables.show()
        tables.map(_.toString.contains("index")).toArray().size should be (6)
      }
    }
  }

}
