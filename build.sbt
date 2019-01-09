ThisBuild / organization := "ru.mitrakov.self"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / name         := "pwd-breaker"

lazy val common      = (project in file("common")).settings()
lazy val api         = (project in file("api")).settings().dependsOn(common)
lazy val dispatcher  = (project in file("dispatcher")).settings().dependsOn(common)
lazy val bruteforcer = (project in file("bruteforcer")).settings().dependsOn(common)
