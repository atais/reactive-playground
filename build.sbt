import sbt.Keys.{scalaVersion, version}

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.6",
  Global / concurrentRestrictions += Tags.limit(Tags.Test, 1),
)

lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
lazy val akka = "com.typesafe.akka" %% "akka-actor" % "2.5.13"
lazy val monix = "io.monix" %% "monix" % "2.3.3"

lazy val playground = (project in file("playground"))
  .settings(
    commonSettings,
    name := "playground",
    libraryDependencies ++= Seq(
      scalatest,
      akka,
      monix
    )
  )
