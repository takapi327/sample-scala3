package library.rds

import doobie.util.log.{ LogHandler, Success, ProcessingFailure, ExecFailure }

import library.util.TrackingLogging

object DoobieLogHandler extends TrackingLogging:
  val trackingLogHandler: LogHandler =
    LogHandler {
      case Success(sql, arguments, e1, e2) => // 成功時は何もしない、必要に応じて以下コメントアウトを解除する
        /*
        trackingLogger.info(s"""Successful Statement Execution:
          |
          | ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
          |
          | arguments = [${arguments.mkString(", ")}]
          |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (${(e1 + e2).toMillis} ms total)
        """.stripMargin)
         */

      case ProcessingFailure(sql, arguments, e1, e2, t) =>
        trackingLogger.error(s"""Failed Resultset Processing:
          |
          | ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
          |
          | arguments = [${arguments.mkString(", ")}]
          |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed) (${(e1 + e2).toMillis} ms total)
          |   failure = ${t.getMessage}
        """.stripMargin)

      case ExecFailure(sql, arguments, e1, t) =>
        trackingLogger.error(s"""Failed Statement Execution:
          |
          | ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
          |
          | arguments = [${arguments.mkString(", ")}]
          |   elapsed = ${e1.toMillis} ms exec (failed)
          |   failure = ${t.getMessage}
        """.stripMargin)
    }
