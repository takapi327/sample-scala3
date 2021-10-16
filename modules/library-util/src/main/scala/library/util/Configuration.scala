package library.util

import com.typesafe.config._

case class Configuration(
  config: Config = ConfigFactory.load()
):

  def get[A](path: String)(using loader: ConfigLoader[A]): A =
    loader.load(config, path)

trait ConfigLoader[A]:
  def load(config: Config, path: String = ""): A

object ConfigLoader:
  def apply[A](f: Config => String => A): ConfigLoader[A] =
    new ConfigLoader[A]:
      def load(config: Config, path: String): A =
        f(config)(path)

  given ConfigLoader[String] = ConfigLoader(_.getString)
