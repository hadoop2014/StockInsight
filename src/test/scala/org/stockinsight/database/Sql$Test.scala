package org.stockinsight.database

import org.scalatest.{Matchers, FlatSpec}
import org.stockinsight.common.LogSupport

/**
 * Created by asus on 2015/8/9.
 */
class Sql$Test extends FlatSpec with LogSupport  with Matchers {

  it should "Sql query " in {
    val dataFrame  = Sql("select * from shanghaiindex limit 100").query
    dataFrame.map(_.mkString(",")).foreach(log.info)
    dataFrame.size should be (100)
  }

}
