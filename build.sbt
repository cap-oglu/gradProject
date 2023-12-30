// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "itu"

//val chiselVersion = "6.0.0-RC1+12-38d69ebb-SNAPSHOT"
val chiselVersion = "5.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "GradProject",
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "5.0.2" % "test",
      
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations",
    ),
    addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full),
    //addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1"),
  )

