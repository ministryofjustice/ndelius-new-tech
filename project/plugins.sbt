ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addDependencyTreePlugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.7")
addSbtPlugin("com.github.sbt" % "sbt-web" % "1.5.8")
addSbtPlugin("com.github.sbt" % "sbt-js-engine" % "1.3.9")
addSbtPlugin("com.github.sbt" % "sbt-mocha" % "2.1.0")
addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.1.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")
addSbtPlugin("io.github.irundaia" % "sbt-sassify" % "1.5.2")
