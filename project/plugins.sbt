ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.22")
addSbtPlugin("com.github.sbt" % "sbt-web" % "1.5.8")
addSbtPlugin("com.github.sbt" % "sbt-js-engine" % "1.3.9")
addSbtPlugin("com.github.sbt" % "sbt-mocha" % "2.0.0")
addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.1.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")
addSbtPlugin("io.github.irundaia" % "sbt-sassify" % "1.5.2")

//addSbtPlugin("com.github.ddispaltro" % "sbt-reactjs" % "0.6.8")
//dependencyOverrides += "org.webjars.npm" % "graceful-readlink" % "1.0.1"
//dependencyOverrides += "org.webjars.npm" % "minimatch" % "3.0.4"