import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

name         := """auth"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play"    % "5.1.0"  % Test,
  "org.scalatest"          %% "scalatest"             % "3.2.12" % "test",
  "io.circe"               %% "circe-core"            % "0.14.2",
  "io.circe"               %% "circe-generic"         % "0.14.2",
  "io.circe"               %% "circe-parser"          % "0.14.2",
  "com.dripower"           %% "play-circe"            % "2814.2",
  "com.github.krzemin"     %% "octopus"               % "0.4.1",
  "com.typesafe.play"      %% "play-slick"            % "5.0.2",
  "com.typesafe.play"      %% "play-slick-evolutions" % "5.0.2",
  "org.postgresql"          % "postgresql"            % "42.3.6",
  "org.mindrot"             % "jbcrypt"               % "0.4",
  "com.github.jwt-scala"   %% "jwt-play-json"         % "9.0.5"
)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerChmodType          := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerExposedPorts       := Seq(9000) //expose default Play port
dockerBaseImage          := "openjdk:11-jre-slim"
dockerRepository         := Some("us.gcr.io/tickets-dev-352019")
dockerUpdateLatest       := true

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
