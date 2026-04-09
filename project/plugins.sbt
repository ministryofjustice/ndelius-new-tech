ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
    Resolver.ivyStylePatterns
)

addDependencyTreePlugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.9")
addSbtPlugin("com.github.sbt" % "sbt-web" % "1.5.8")
addSbtPlugin("com.github.sbt" % "sbt-js-engine" % "1.3.9")
addSbtPlugin("com.github.sbt" % "sbt-mocha" % "2.1.0")
addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.1.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")
addSbtPlugin("uk.gov.hmrc" % "sbt-sass-compiler" % "0.13.0")
