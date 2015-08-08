package org.stockinsight.common

import com.typesafe.config.ConfigFactory

/**
 * Created by asus on 2015/8/8.
 */
object LoadConfig {

  def getString(key: String): String = ConfigFactory.load("default.conf").getString(key)

}
