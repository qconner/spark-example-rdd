import Dependencies.allSpecified

ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "example"
ThisBuild / organizationName := "Quentin Conner"

lazy val assemblySettings = Seq(
  // jar name
  assemblyJarName in assembly := ((name) map { (n) => n + ".jar" }).value,

  // don't run test
  test in assembly := {},

  // merge strategies
  assemblyMergeStrategy in assembly := {
    case "log4j.properties"                                    => MergeStrategy.concat
    case PathList("javax", xs @ _*)                            => MergeStrategy.last
    case PathList("org", "slf4j", "impl", xs @ _*)             => MergeStrategy.last
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

lazy val root = (project in file("."))
  .settings(
    name := "clf-ip-url-top-N",
    libraryDependencies ++= allSpecified,    // see project/Dependencies.scala
    assemblySettings
  )

