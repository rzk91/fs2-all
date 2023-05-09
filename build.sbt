ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

val fs2Version = "3.6.1"
val fs2KafkaVersion = "3.0.0-M8"
val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .settings(
    name             := "fs2-kafka",
    idePackagePrefix := Some("com.rzk.fs2"),
    libraryDependencies ++= fs2Dependencies ++ circeDependencies ++ otherDependencies
  )

val fs2Dependencies = Seq(
  "co.fs2"          %% "fs2-core"  % fs2Version,
  "co.fs2"          %% "fs2-io"    % fs2Version,
  "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion
)

val circeDependencies = Seq(
  "io.circe" %% "circe-core"           % circeVersion,
  "io.circe" %% "circe-generic"        % circeVersion,
  "io.circe" %% "circe-parser"         % circeVersion,
  "io.circe" %% "circe-optics"         % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion
)

val otherDependencies = Seq(
  "com.github.pureconfig"      %% "pureconfig"    % "0.17.2",
  "org.slf4j"                   % "slf4j-log4j12" % "2.0.5",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.apache.commons"          % "commons-lang3" % "3.12.0",
  "commons-io"                  % "commons-io"    % "2.11.0"
)

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-Ymacro-annotations",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)
