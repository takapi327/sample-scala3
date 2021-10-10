package library.util

import com.typesafe.config._

object Configuration:
  val config = ConfigFactory.load()
  