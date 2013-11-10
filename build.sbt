name := "mockito-scala-example"

organization := "bleibinhaus"

version := "0.1"

scalaVersion := "2.10.3"

resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.2" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"
)

initialCommands := "import bleibinhaus.mockitoscalaexample._"

