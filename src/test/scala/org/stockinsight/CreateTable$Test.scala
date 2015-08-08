package org.stockinsight

import org.scalatest.{Matchers, FlatSpec}
import org.stockinsight.common.LogSupport

/**
 * Created by asus on 2015/8/5.
 */
class CreateTable$Test extends FlatSpec with LogSupport with Matchers{

  it should "create table by spark sql with hivecontext" in{
    CreateTable
  }

}
