name := "mockito-scala-example"

organization := "bleibinhaus"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.2" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

initialCommands := "import bleibinhaus.mockitoscalaexample._"

