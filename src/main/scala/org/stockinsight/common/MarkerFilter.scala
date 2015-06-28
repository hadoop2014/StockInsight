package org.stockinsight.common

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.AbstractMatcherFilter
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.{Marker, MarkerFactory}

class MarkerFilter extends  AbstractMatcherFilter[ILoggingEvent] {

  var markerToMatch: Marker = null

  override def start(): Unit = this.markerToMatch match {
    case null => addError("!!! no marker yet !!!")
    case _ => super.start()
  }


  def decide(event: ILoggingEvent): FilterReply = {
    val marker: Marker = event.getMarker()
    if (!isStarted())
      return FilterReply.NEUTRAL
    if (null == marker)
      return onMismatch
    if (markerToMatch.contains(marker))
      return onMatch
    return onMismatch
  }

  def setMarker(markerStr: String): Unit = {
    if (null != markerStr) markerToMatch = MarkerFactory.getMarker(markerStr)
  }

}


