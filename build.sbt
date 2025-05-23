import com.typesafe.config.ConfigFactory
import com.typesafe.sbt.jse.SbtJsEngine.autoImport.JsEngineKeys._
import com.typesafe.sbt.jse.SbtJsTask.executeJs
import com.typesafe.sbt.web.incremental
import com.typesafe.sbt.web.incremental.{OpInputHash, OpInputHasher, OpResult, OpSuccess}

import scala.concurrent.duration._

val conf = ConfigFactory.parseFile(new File("conf/application.conf"))

name := "ndelius2"

organization := "uk.gov.justice.digital"

version := sys.env.getOrElse("APP_VERSION",
  conf.getString("app.version") + sys.env.getOrElse("CIRCLE_BUILD_NUM", "SNAPSHOT"))

lazy val root = (project in file(".")).enablePlugins(PlayJava).configs( IntegrationTest )

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
MochaKeys.requires += "setup.js"

resolvers ++= Seq("Spring Release Repository" at "https://repo.spring.io/plugins-release")

scalaVersion := "2.13.16"
pipelineStages := Seq(digest)
libraryDependencies ++= Seq(
  guice,
  filters,
  javaWs,
  ehcache,
  "org.webjars" %% "webjars-play" % "2.8.18",
  "org.webjars.bower" % "chartjs" % "2.6.0",
  "org.webjars" % "underscorejs" % "1.8.3",
  "org.webjars" % "jquery" % "1.12.4",
  "org.webjars" % "jquery-ui" % "1.12.1",
  "org.mongodb" % "mongodb-driver-rx" % "1.4.0",
  "com.pauldijou" %% "jwt-core" % "3.0.1",
  "commons-io" % "commons-io" % "2.6",
  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.9.1",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.0.1",
  "com.github.coveo" % "uap-java" % "1.3.1-coveo1",
  "com.amazonaws" % "aws-java-sdk" % "1.11.46",

  "org.projectlombok" % "lombok" % "1.18.38" % "provided",

  "org.assertj" % "assertj-core" % "3.8.0" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.12.0" % "test",
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.4.0" % "test",
  "info.cukes" % "cucumber-guice" % "1.1.5" % "test",
  "info.cukes" % "cucumber-java" % "1.2.2" % "test",
  "info.cukes" % "cucumber-junit" % "1.2.2" % "test",

  ("org.languagetool" % "language-en" % "4.8").exclude("com.typesafe.akka", "akka-actor_2.11")
)

excludeDependencies ++= Seq(
  ExclusionRule("commons-logging", "commons-logging")
)

// Wiremock only works with this older version of Jetty
//val jettyVersion = "9.2.22.v20170606"
//dependencyOverrides ++= Set(
//  "org.eclipse.jetty" % "jetty-server" % jettyVersion,
//  "org.eclipse.jetty" % "jetty-client" % jettyVersion,
//  "org.eclipse.jetty" % "jetty-http" % jettyVersion,
//  "org.eclipse.jetty" % "jetty-io" % jettyVersion,
//  "org.eclipse.jetty" % "jetty-util" % jettyVersion
//)

assembly / mainClass := Some("play.core.server.ProdServerStart")

assembly / fullClasspath += Attributed.blank(PlayKeys.playPackageAssets.value)

assembly / assemblyMergeStrategy := {
  case playWs if playWs.contains("play/api/libs/ws/package") || playWs.endsWith("reference-overrides.conf") => MergeStrategy.last
  case PathList(ps @ _*) if ps.contains("jna") => MergeStrategy.first
  case PathList(ps @ _*) if ps.contains("minlog") => MergeStrategy.first
  case other => (assemblyMergeStrategy in assembly).value(other)
}

assembly / assemblyJarName := "ndelius2-" + version.value + ".jar"

val browserifyTask = taskKey[Seq[File]]("Run browserify")
val browserifyOutputDir = settingKey[File]("Browserify output directory")
browserifyOutputDir := target.value / "web" / "browserify"

browserifyTask := {
  val sourceDir = (Assets / sourceDirectory).value / "javascripts"

  implicit val fileHasherIncludingOptions: OpInputHasher[File] =
    OpInputHasher[File](f => OpInputHash.hashString(f.getCanonicalPath))
  val sources = (sourceDir ** ((Assets / browserifyTask / includeFilter).value -- DirectoryFilter)).get
  val outputFile = browserifyOutputDir.value / "bundle.js"
  val outputFile2 = browserifyOutputDir.value / "reports.js"

  val results = incremental.syncIncremental((Assets / streams).value.cacheDirectory / "run", sources) {
    modifiedSources: Seq[File] =>
      if (modifiedSources.nonEmpty) {
        ( Assets / npmNodeModules ).value
        val inputFile = baseDirectory.value / "app/assets/javascripts/index.js"
        val inputFile2 = baseDirectory.value / "app/assets/javascripts/app.js"
        val modules =  (baseDirectory.value / "node_modules").getAbsolutePath
        browserifyOutputDir.value.mkdirs
        executeJs(state.value,
          engineType.value,
          None,
          Seq(modules),
          baseDirectory.value / "browserify.js",
          Seq(inputFile.getPath, outputFile.getPath),
          60.seconds)
        ()
        executeJs(state.value,
          engineType.value,
          None,
          Seq(modules),
          baseDirectory.value / "browserify.js",
          Seq(inputFile2.getPath, outputFile2.getPath),
          60.seconds)
        ()
      }

      val opResults: Map[File, OpResult] =
        modifiedSources.map(file => (file, OpSuccess(Set(file), Set(outputFile)))).toMap
      (opResults, List(outputFile, outputFile2))
  }(fileHasherIncludingOptions)

  results._2
}

Assets / sourceGenerators +=  browserifyTask.taskValue
Assets / resourceDirectories += browserifyOutputDir.value
IntegrationTest / unmanagedResourceDirectories += baseDirectory.value  / "target/web/public/test"
Test / unmanagedResourceDirectories += baseDirectory.value / "target/web/public/test"
Test / unmanagedResourceDirectories += baseDirectory.value / "features"

assembly := (assembly dependsOn ( Assets / npmNodeModules )).value