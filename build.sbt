name := "actors-dojo"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1"
)
