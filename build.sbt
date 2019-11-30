name := "petstore"

version := "0.1"

scalaVersion := "2.13.1"

mainClass in Compile := Some("petstore.Main")

fork in run := true
cancelable in Global := true

javaOptions ++= Seq("-Xms64m", "-Xmx96m")

scalacOptions := Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "com.h2database" % "h2" % "1.4.196",
  "com.lihaoyi" %% "cask" % "0.3.6",
  "net.liftweb" %% "lift-json" % "3.4.0"
)