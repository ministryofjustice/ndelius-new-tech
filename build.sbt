import com.typesafe.config.ConfigFactory
import com.typesafe.sbt.jse.SbtJsEngine.autoImport.JsEngineKeys.*
import com.typesafe.sbt.jse.SbtJsTask.executeJs
import com.typesafe.sbt.web.incremental
import com.typesafe.sbt.web.incremental.{OpInputHash, OpInputHasher, OpResult, OpSuccess}

import scala.concurrent.duration.*

val conf = ConfigFactory.parseFile(new File("conf/application.conf"))

name := "ndelius2"

organization := "uk.gov.justice.digital"

version := sys.env.getOrElse("APP_VERSION",
  conf.getString("app.version") + sys.env.getOrElse("CIRCLE_BUILD_NUM", "SNAPSHOT"))

lazy val root = (project in file(".")).enablePlugins(PlayJava, SbtWeb, SbtJsEngine).configs( IntegrationTest )

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

resolvers ++= Seq("Spring Release Repository" at "https://repo.spring.io/plugins-release")

scalaVersion := "2.13.16"
pipelineStages := Seq(digest)
libraryDependencies ++= Seq(
  guice,
  filters,
  javaWs,
  "org.webjars" %% "webjars-play" % "2.9.1",
  "org.webjars.bower" % "chartjs" % "2.6.0",
  "org.webjars" % "underscorejs" % "1.13.0-2",
  "org.webjars" % "jquery" % "3.7.1",
  "org.webjars" % "jquery-ui" % "1.14.1",
  "org.mongodb" % "mongodb-driver-rx" % "1.5.0",
  "commons-io" % "commons-io" % "2.19.0",
  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.24.3",
  "com.github.ua-parser" % "uap-java" % "1.6.1",
  "org.languagetool" % "language-en" % "6.6",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.4",

  "org.projectlombok" % "lombok" % "1.18.38" % "provided",

  "org.assertj" % "assertj-core" % "3.27.3" % "test",
  "org.mockito" %% "mockito-scala" % "1.17.45" % "test",
  "org.wiremock" % "wiremock" % "3.13.0" % "test",
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % "4.33.0" % "test",
  "io.github.bonigarcia" % "webdrivermanager" % "6.1.0",
  "io.cucumber" % "cucumber-guice" % "7.23.0" % "test",
  "io.cucumber" % "cucumber-java" % "7.23.0" % "test",
  "io.cucumber" % "cucumber-junit" % "7.23.0" % "test"
)

dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.18.4",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.18.4",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.18.4",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.18.4",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.18.4",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.18.4",
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % "2.18.4",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.18.4",
)

excludeDependencies ++= Seq(
  ExclusionRule("commons-logging", "commons-logging")
)

assembly / mainClass := Some("play.core.server.ProdServerStart")
assembly / fullClasspath += Attributed.blank(PlayKeys.playPackageAssets.value)

assembly / assemblyMergeStrategy := {
//  case playWs if playWs.contains("play/api/libs/ws/package") || playWs.endsWith("reference-overrides.conf") => MergeStrategy.last
  case path if path.contains("reference-overrides.conf") => MergeStrategy.concat
  case path if path.contains("module-info.class") => MergeStrategy.concat
  case path if path.contains("jna") => MergeStrategy.first
  case path if path.contains("minlog") => MergeStrategy.first
  case path if path.contains("xml") => MergeStrategy.first
  case path if path.contains("javax") => MergeStrategy.first
  case path if path.contains("jakarta") => MergeStrategy.first
  case path if path.contains("istack") => MergeStrategy.first
  case path if path.contains("protobuf") => MergeStrategy.first
  case path if path.contains("mailcap.default") => MergeStrategy.first
  case path if path.contains("io.netty.versions.properties") => MergeStrategy.first
  case path if path.contains("mimetypes.default") => MergeStrategy.first
  case other => (assembly / assemblyMergeStrategy).value(other)
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
  val outputFile = browserifyOutputDir.value / "reports.js"

  val results = incremental.syncIncremental((Assets / streams).value.cacheDirectory / "run", sources) {
    modifiedSources: Seq[File] =>
      if (modifiedSources.nonEmpty) {
        ( Assets / npmNodeModules ).value
        val inputFile = baseDirectory.value / "app/assets/javascripts/app.js"
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
      }

      val opResults: Map[File, OpResult] =
        modifiedSources.map(file => (file, OpSuccess(Set(file), Set(outputFile)))).toMap
      (opResults, List(outputFile))
  }(fileHasherIncludingOptions)

  results._2
}

Assets / sourceGenerators +=  browserifyTask.taskValue
Assets / resourceDirectories += browserifyOutputDir.value
IntegrationTest / unmanagedResourceDirectories += baseDirectory.value  / "target/web/public/test"
Test / unmanagedResourceDirectories += baseDirectory.value / "target/web/public/test"
Test / unmanagedResourceDirectories += baseDirectory.value / "features"

assembly := (assembly dependsOn ( Assets / npmNodeModules )).value