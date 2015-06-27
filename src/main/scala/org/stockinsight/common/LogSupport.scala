package org.stockinsight.common

import org.slf4j.{MarkerFactory, LoggerFactory}

/**
 * Created by asus on 2015/6/28.
 */
trait LogSupport {
  protected val log = LoggerFactory.getLogger(this.getClass)
  protected val BusinessMarker = MarkerFactory.getMarker("BusinessMarker")
  protected val DebugMarker = MarkerFactory.getMarker("DebugMarker")
}
