
ThisBuild / version      := "1.0.0"
ThisBuild / organization := "io.github.takapi327"
ThisBuild / scalaVersion := "3.0.1"
ThisBuild / startYear    := Some(2021)

val doobieVersion = "1.0.0-RC1"
val catsVersion   = "2.6.1"

lazy val root = (project in file("."))
  .settings(
    name := "sample-scala3",
    commonSettings,
    libraryDependencies ++= Seq(
      "mysql"          %  "mysql-connector-java" % "8.0.26",
      "com.novocode"   %  "junit-interface"      % "0.11" % "test",
      "org.typelevel"  %% "cats-core"            % catsVersion
    )
  )
  .aggregate(libraryRds, libraryUtil)
  .dependsOn(libraryRds, libraryUtil)

lazy val libraryRds = (project in file("modules/library-rds"))
  .settings(
    name := "library-rds",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % catsVersion,
      "org.tpolecat"  %% "doobie-core" % doobieVersion,
      "co.fs2"        %% "fs2-core"    % "3.1.3"
    )
  )
  .aggregate(libraryUtil)
  .dependsOn(libraryUtil)

lazy val libraryUtil = (project in file("modules/library-util"))
  .settings(
    name := "library-util",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe" % "config"        % "1.4.1",
      "org.slf4j"    % "slf4j-log4j12" % "1.7.32"
    )
  )

lazy val commonSettings = Seq(
  run / fork := true,

  javaOptions ++= Seq(
    "-Dconfig.file=conf/env.dev/application.conf",
    "-Dlogger.file=conf/env.dev/logback.xml"
  ),

  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
  )
)