package library.util

import org.slf4j.MarkerFactory
import org.slf4j.LoggerFactory
import org.slf4j.spi.LocationAwareLogger

import java.util.UUID

trait TrackingLogging:
  val trackingLogger: TrackingLogger =
    TrackingLogger(LoggerFactory.getLogger(getClass).asInstanceOf[LocationAwareLogger])

import LocationAwareLogger._
protected case class TrackingLogger(logger: LocationAwareLogger):

  /**
   * Message only log
   */
  def info(message:  => String): Unit = { if (logger.isInfoEnabled)  logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, INFO_INT,  message, null, null) }
  def warn(message:  => String): Unit = { if (logger.isWarnEnabled)  logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, WARN_INT,  message, null, null) }
  def error(message: => String): Unit = { if (logger.isErrorEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, ERROR_INT, message, null, null) }
  def debug(message: => String): Unit = { if (logger.isDebugEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, DEBUG_INT, message, null, null) }
  def trace(message: => String): Unit = { if (logger.isTraceEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, TRACE_INT, message, null, null) }

  /**
   * Handle messages and exceptions
   */
  def info(message:  => String, throwable: Throwable): Unit = { if (logger.isInfoEnabled)  logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, INFO_INT,  message, null, throwable) }
  def warn(message:  => String, throwable: Throwable): Unit = { if (logger.isWarnEnabled)  logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, WARN_INT,  message, null, throwable) }
  def error(message: => String, throwable: Throwable): Unit = { if (logger.isErrorEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, ERROR_INT, message, null, throwable) }
  def debug(message: => String, throwable: Throwable): Unit = { if (logger.isDebugEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, DEBUG_INT, message, null, throwable) }
  def trace(message: => String, throwable: Throwable): Unit = { if (logger.isTraceEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, TRACE_INT, message, null, throwable) }

  /**
   * Handle messages and Array Object
   */
  def info(message:  => String, argArray: Array[Object]): Unit = { if (logger.isInfoEnabled)  logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, INFO_INT,  message, argArray, null) }
  def warn(message:  => String, argArray: Array[Object]): Unit = { if (logger.isWarnEnabled)  logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, WARN_INT,  message, argArray, null) }
  def error(message: => String, argArray: Array[Object]): Unit = { if (logger.isErrorEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, ERROR_INT, message, argArray, null) }
  def debug(message: => String, argArray: Array[Object]): Unit = { if (logger.isDebugEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, DEBUG_INT, message, argArray, null) }
  def trace(message: => String, argArray: Array[Object]): Unit = { if (logger.isTraceEnabled) logger.log(MarkerFactory.getMarker(UUID.randomUUID.toString().replace("-", "")), getClass.getName, TRACE_INT, message, argArray, null) }
