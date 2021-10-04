
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
  .aggregate(library)
  .dependsOn(library)

lazy val library = (project in file("modules/library-rds"))
  .settings(
    name := "library-rds",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % catsVersion,
      "org.tpolecat"  %% "doobie-core" % doobieVersion,
    )
  )

lazy val commonSettings = Seq(
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