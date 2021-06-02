name := "ScalaExercises"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
libraryDependencies += "com.47deg" %% "scalacheck-toolbox-datetime" % "0.5.0"
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full)