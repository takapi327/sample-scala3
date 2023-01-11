
ThisBuild / version      := "1.0.0"
ThisBuild / organization := "io.github.takapi327"
ThisBuild / scalaVersion := "3.1.3"
ThisBuild / startYear    := Some(2021)

lazy val doobieVersion = "1.0.0-RC1"
lazy val catsVersion   = "2.6.1"
lazy val effVersion    = "5.21.0"
lazy val http4sVersion = "0.23.6"
lazy val jjwtVersion   = "0.11.5"

lazy val root = (project in file("."))
  .settings(
    name := "sample-scala3",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-effect"          % "3.3.3",
      "com.google.inject" %  "guice"                % "5.1.0",
      "mysql"             %  "mysql-connector-java" % "8.0.26",
      "com.novocode"      %  "junit-interface"      % "0.11" % "test",
      "io.jsonwebtoken"   %  "jjwt-api"             % jjwtVersion,
      "io.jsonwebtoken"   %  "jjwt-impl"            % jjwtVersion % "runtime",
      "io.jsonwebtoken"   %  "jjwt-jackson"         % jjwtVersion,
      "org.tpolecat"      %% "doobie-core"          % doobieVersion,
      "org.typelevel"     %% "cats-core"            % catsVersion,
      "co.fs2"            %% "fs2-core"             % "3.1.3",
      "org.http4s"        %% "http4s-dsl"           % http4sVersion,
      "org.http4s"        %% "http4s-blaze-server"  % http4sVersion,
      "org.http4s"        %% "http4s-blaze-client"  % http4sVersion,
      "org.http4s"        %% "http4s-circe"         % http4sVersion,
      "org.http4s"        %% "http4s-ember-server"  % "0.23.14"
    )
  )
  .aggregate(libraryRds, libraryUtil)
  .dependsOn(libraryRds, libraryUtil)

lazy val libraryRds = (project in file("modules/library-rds"))
  .settings(
    name := "library-rds",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"       % catsVersion,
      "org.tpolecat"  %% "doobie-core"     % doobieVersion,
      "org.tpolecat"  %% "doobie-hikari"   % doobieVersion,
      "co.fs2"        %% "fs2-core"        % "3.1.3",
      "com.zaxxer"    %  "HikariCP"        % "5.0.0",
      "org.atnos"     %% "eff"             % effVersion,
      "org.atnos"     %% "eff-cats-effect" % effVersion,
      "org.atnos"     %% "eff-doobie"      % effVersion,
      "com.google.inject" % "guice" % "5.0.1"
    )
  )
  .aggregate(libraryUtil)
  .dependsOn(libraryUtil)

lazy val libraryUtil = (project in file("modules/library-util"))
  .settings(
    name := "library-util",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe"   % "config"          % "1.4.1",
      "org.slf4j"      % "slf4j-api"       % "2.0.0-alpha1",
      "ch.qos.logback" % "logback-classic" % "1.3.0-alpha4"
    )
  )

lazy val lepusAuth = (project in file("app/lepus-auth"))
  .settings(name := "lepus-auth")
  .settings(
    scalaVersion := "3.2.0",
    run / fork := true,
    javaOptions ++= Seq(
      "-Dconfig.file=conf/env.dev/application.conf",
      "-Dlogback.configurationFile=conf/env.dev/logback.xml"
    ),
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-feature",
      "utf8",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions"
    ),
    libraryDependencies ++= Seq(
      "com.auth0" % "mvc-auth-commons" % "1.9.3",
      "com.nimbusds" % "oauth2-oidc-sdk" % "10.4",
      "io.chrisdavenport" %% "mapref" % "0.2.1",
      "ch.qos.logback" % "logback-classic" % "1.4.5",
      "io.jsonwebtoken" % "jjwt-api" % jjwtVersion,
      lepusLogger
    )
  )
  .enablePlugins(Lepus)

val v = "0.23.11-1437-17c0c99-20230106T064312Z-SNAPSHOT"
lazy val http4sExample = (project in file("app/http4s-example"))
  .settings(name := "http4s-example")
  .settings(
    scalaVersion := "3.2.0",
    run / fork := true,
    javaOptions ++= Seq(
      "-Dconfig.file=conf/env.dev/application.conf",
      "-Dlogback.configurationFile=conf/env.dev/logback.xml"
    ),
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-feature",
      "utf8",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions"
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.5",
      "org.http4s" %% "http4s-dsl" % v,
      "org.http4s" %% "http4s-ember-server" % v,
    )
  )

lazy val ldbc = (project in file("modules/ldbc"))
  .settings(name := "ldbc")
  .settings(scalaVersion := "3.2.0")
  .settings(
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-feature",
      "utf8",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions"
    ),
    resolvers += "Lepus Maven" at "s3://com.github.takapi327.s3-ap-northeast-1.amazonaws.com/lepus/",
    libraryDependencies ++= Seq(
      lepusCore,
      "org.typelevel" %% "cats-effect" % "3.4.4",
      "mysql" %  "mysql-connector-java" % "8.0.26"
    )
  )

lazy val commonSettings = Seq(
  run / fork := true,

  javaOptions ++= Seq(
    "-Dconfig.file=conf/env.dev/application.conf",
    "-Dlogback.configurationFile=conf/env.dev/logback.xml"
  ),

  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _))                  => Seq("-Ykind-projector:underscores")
    case Some((2, 13)) | Some((2, 12)) => Seq("-Xsource:3", "-P:kind-projector:underscore-placeholders")
    case _ => Seq.empty
  })
)
