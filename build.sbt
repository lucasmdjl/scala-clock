ThisBuild / organization := "dev.lucasmdjl"
ThisBuild / version := "0.2.0"
ThisBuild / description := "A Scala 3 micro-library for intelligent caching with background refresh capabilities."
ThisBuild / scalaVersion := "3.7.1"
ThisBuild / sonatypeCredentialHost := xerial.sbt.Sonatype.sonatypeCentralHost

lazy val root = (project in file("."))
  .settings(
    name := "scala-clock",
    idePackagePrefix := Some("dev.lucasmdjl.clock"),
      libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scalacheck" %% "scalacheck" % "1.18.1" % "test",
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % "test"
    ),
    publishTo := sonatypePublishToBundle.value,
  )
