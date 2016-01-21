name := "actors-dojo"
organization := "bbc"

version := "1.0"

scalaVersion := "2.11.7"

lazy val versions = new {
  val scalatest = "2.2.4"
  val specs2 = "2.3.12"
  val akka = "2.4.1"
}

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % versions.scalatest % "test",
  "org.specs2" %% "specs2" % versions.specs2 % "test",
  "com.typesafe.akka" %% "akka-actor" % versions.akka
)