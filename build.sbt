val scala3Version = "3.0.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "sample-scala3",
    version := "0.1.0",

    scalaVersion := scala3Version,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
