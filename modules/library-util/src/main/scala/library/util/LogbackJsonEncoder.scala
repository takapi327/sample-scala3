package mvc.util

import ch.qos.logback.core.LayoutBase
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.classic.spi.ILoggingEvent

class LogbackJsonEncoder extends LayoutBase[ILoggingEvent]:

  def doLayout(event: ILoggingEvent): String = {
    Map(
      "timeStamp"        -> event.getTimeStamp,
      "level"            -> event.getLevel.toString,
      "maker"            -> event.getMarker.toString,
      "thread"           -> event.getThreadName,
      "message"          -> event.getMessage,
      "throwable"        -> event.getThrowableProxy.toString,
      "class"            -> event.getClass.toString,
      "argument"         -> event.getArgumentArray.toString,
      "callerData"       -> event.getCallerData.toString,
      "loggerContextVO"  -> event.getLoggerContextVO.toString,
      "formattedMessage" -> event.getFormattedMessage,
      "mdcPropertyMap"   -> event.getMDCPropertyMap.toString,
      "loggerName"       -> event.getLoggerName,
      "coreConstants"    -> CoreConstants.LINE_SEPARATOR
    ).toString()
  }
