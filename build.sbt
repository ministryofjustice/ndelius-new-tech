import com.typesafe.config.ConfigFactory
import com.typesafe.sbt.jse.SbtJsEngine.autoImport.JsEngineKeys._
import com.typesafe.sbt.jse.SbtJsTask.executeJs
import scala.concurrent.duration._

val conf = ConfigFactory.parseFile(new File("conf/application.conf"))

name := "ndelius2"

organization := "uk.gov.justice.digital"

version := conf.getString("app.version")

lazy val root = (project in file(".")).enablePlugins(PlayJava, SbtWeb, SbtJsEngine)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
MochaKeys.requires += "setup.js"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  guice,
  filters,
  javaWs,
  "org.webjars" %% "webjars-play" % "2.6.1",
  "org.webjars" % "es5-shim" % "4.5.9",
  "org.webjars.bower" % "react" % "0.14.9", // https://facebook.github.io/react/blog/2016/01/12/discontinuing-ie8-support.html
  "org.webjars.bower" % "chartjs" % "2.6.0",
  "org.webjars" % "excanvas" % "3",         // Canvas support for IE8
  "org.webjars" % "underscorejs" % "1.8.3",
  "org.webjars" % "jquery" % "1.12.4",
  "org.webjars" % "jquery-ui" % "1.12.1",
  "org.mongodb" % "mongodb-driver-rx" % "1.4.0",
  "org.languagetool" % "language-en" % "3.7",

  "org.projectlombok" % "lombok" % "1.16.16" % "provided",

  "org.assertj" % "assertj-core" % "3.8.0" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.12.0" % "test",
  "org.apache.opennlp" % "opennlp-tools" % "1.8.2"
)

excludeDependencies ++= Seq(
  SbtExclusionRule("commons-logging", "commons-logging")
)

// Wiremock only works with this older version of Jetty
val jettyVersion = "9.2.22.v20170606"
dependencyOverrides ++= Set(
  "org.eclipse.jetty" % "jetty-server" % jettyVersion,
  "org.eclipse.jetty" % "jetty-client" % jettyVersion,
  "org.eclipse.jetty" % "jetty-http" % jettyVersion,
  "org.eclipse.jetty" % "jetty-io" % jettyVersion,
  "org.eclipse.jetty" % "jetty-util" % jettyVersion
)

mainClass in assembly := Some("play.core.server.ProdServerStart")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

assemblyMergeStrategy in assembly := {
  case playWs if playWs.contains("play/api/libs/ws/package") || playWs.endsWith("reference-overrides.conf") => MergeStrategy.last
  case other => (assemblyMergeStrategy in assembly).value(other)
}

assemblyJarName in assembly := "ndelius2-" + version.value + ".jar"

val browserifyTask = taskKey[Seq[File]]("Run browserify")
val browserifyOutputDir = settingKey[File]("Browserify output directory")
browserifyOutputDir := target.value / "web" / "browserify"

browserifyTask := {
  println("Running browserify")
  ( npmNodeModules in Assets ).value
  val inputFile = baseDirectory.value / "app/assets/javascripts/index.js"
  val outputFile = browserifyOutputDir.value / "bundle.js"
  browserifyOutputDir.value.mkdirs
  val modules =  (baseDirectory.value / "node_modules").getAbsolutePath
  executeJs(state.value,
    engineType.value,
    None,
    Seq(modules),
    baseDirectory.value / "browserify.js",
    Seq(inputFile.getPath, outputFile.getPath),
    30.seconds)
  ()

  List(outputFile)
}

sourceGenerators in Assets +=  browserifyTask.taskValue
resourceDirectories in Assets += browserifyOutputDir.value
