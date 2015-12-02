scalaVersion := "2.10.5"

crossScalaVersions := Seq("2.10.5", "2.11.7")

net.virtualvoid.sbt.graph.Plugin.graphSettings

// Disable parallel execution, the various Druid oriented tests need to claim ports
parallelExecution in ThisBuild := false

// Disable parallel execution, the various Druid oriented tests need to claim ports
parallelExecution in Test := false

concurrentRestrictions in Global += Tags.limitAll(1)

val jacksonOneVersion = "1.9.13"
val jacksonTwoVersion = "2.6.1"
val druidVersion = "0.8.1"
val finagleVersion = "6.25.0"
val twitterUtilVersion = "6.25.0"
val samzaVersion = "0.8.0"
val sparkVersion = "1.4.1"

val coreDependencies = Seq(
  "com.metamx" %% "scala-util" % "1.11.6" exclude("log4j", "log4j") force(),
  "com.metamx" % "java-util" % "0.27.4" exclude("log4j", "log4j") force(),
  "io.netty" % "netty" % "3.10.4.Final" force(),
  "com.twitter" %% "util-core" % twitterUtilVersion,
  "com.twitter" %% "finagle-core" % finagleVersion,
  "com.twitter" %% "finagle-http" % finagleVersion,
  "org.slf4j" % "slf4j-api" % "1.7.12" force() force(),
  "org.slf4j" % "jul-to-slf4j" % "1.7.12" force() force(),

  // Curator uses Jackson 1.x internally, and older version cause problems with service discovery.
  "org.codehaus.jackson" % "jackson-core-asl" % jacksonOneVersion force(),
  "org.codehaus.jackson" % "jackson-mapper-asl" % jacksonOneVersion force(),

  // We use Jackson 2.x internally (and so does Druid).
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonTwoVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonTwoVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonTwoVersion,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-smile" % jacksonTwoVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonTwoVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonTwoVersion
) ++ Seq(
  "io.druid" % "druid-server" % druidVersion
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("log4j", "log4j")
    exclude("org.apache.logging.log4j", "log4j-core")
    exclude("org.apache.logging.log4j", "log4j-api")
    exclude("org.apache.logging.log4j", "log4j-slf4j-impl")
    exclude("org.apache.logging.log4j", "log4j-1.2-api")
    exclude("com.lmax", "disruptor") // Pulled in by log4j2, conflicts with the one Storm wants.
    force(),
  "io.druid" % "druid-indexing-service" % druidVersion
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("log4j", "log4j")
    exclude("org.apache.logging.log4j", "log4j-core")
    exclude("org.apache.logging.log4j", "log4j-api")
    exclude("org.apache.logging.log4j", "log4j-slf4j-impl")
    exclude("org.apache.logging.log4j", "log4j-1.2-api")
    exclude("com.lmax", "disruptor") // Pulled in by log4j2, conflicts with the one Storm wants.
    force(),
  "com.google.inject" % "guice" % "4.0-beta" force(),
  "com.google.inject.extensions" % "guice-servlet" % "4.0-beta" force(),
  "com.google.inject.extensions" % "guice-multibindings" % "4.0-beta" force(),
  "javax.validation" % "validation-api" % "1.1.0.Final" force()
)

val stormDependencies = Seq(
  "org.apache.storm" % "storm-core" % "0.9.3" % "optional"
    exclude("javax.jms", "jms")
    exclude("ch.qos.logback", "logback-classic")
    exclude("org.slf4j", "log4j-over-slf4j")
    force(),
  "com.twitter" %% "chill" % "0.7.1" % "optional"
)

val samzaDependencies = Seq(
  "org.apache.samza" % "samza-api" % samzaVersion % "optional"
)

val sparkDependencies = Seq (
  "org.apache.spark" % "spark-streaming_2.10" % sparkVersion % "optional"
    exclude("org.slf4j", "log4j-over-slf4j")
    exclude("org.slf4j", "slf4j-log4j12")
    force()
)

val coreTestDependencies = Seq(
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "io.druid" % "druid-services" % druidVersion % "test"
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("ch.qos.logback", "logback-classic")
    exclude("log4j", "log4j")
    exclude("org.apache.logging.log4j", "log4j-core")
    exclude("org.apache.logging.log4j", "log4j-api")
    exclude("org.apache.logging.log4j", "log4j-slf4j-impl")
    exclude("org.apache.logging.log4j", "log4j-jul")
    exclude("org.apache.logging.log4j", "log4j-1.2-api")
    exclude("com.lmax", "disruptor") // Pulled in by log4j2, conflicts with the one Storm wants.
    force(),
  "org.apache.curator" % "curator-test" % "2.6.0" % "test" exclude("log4j", "log4j") force(),
  "com.sun.jersey" % "jersey-servlet" % "1.17.1" % "test" force(),
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.11-RC1" % "test",
  "ch.qos.logback" % "logback-core" % "1.1.2" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2" % "test",
  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.4" % "test",
  "org.apache.logging.log4j" % "log4j-api" % "2.4" % "test",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12" % "test",
  "org.slf4j" % "jul-to-slf4j" % "1.7.12" % "test"
)

// Force 2.10 here, makes update resolution happy, but since w'ere not building for 2.11
// we won't end up in runtime version hell by doing this.
val samzaTestDependencies = Seq(
  "org.apache.samza" % "samza-core_2.10" % samzaVersion % "test"
)

lazy val commonSettings = Seq(
  organization := "io.druid",

  scalacOptions := Seq("-feature", "-deprecation"),

  licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),

  homepage := Some(url("https://github.com/druid-io/tranquility")),

  publishMavenStyle := true,

  publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"),

  pomIncludeRepository := { _ => false },

  pomExtra := (
    <scm>
      <url>https://github.com/druid-io/tranquility.git</url>
      <connection>scm:git:git@github.com:druid-io/tranquility.git</connection>
    </scm>
      <developers>
        <developer>
          <name>Gian Merlino</name>
          <organization>Druid Project</organization>
          <organizationUrl>http://druid.io/</organizationUrl>
        </developer>
      </developers>),

  fork in Test := true,

  // storm-core has a package and object with the same name
  scalacOptions += "-Yresolve-term-conflict:object"
) ++ releaseSettings ++ Seq(
  ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value
)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false)
  .aggregate(core, spark, storm, samza)

lazy val core = project.in(file("core"))
  .settings(commonSettings: _*)
  .settings(name := "tranquility-core")
  .settings(publishArtifact in(Test, packageBin) := true)
  .settings(libraryDependencies ++= (coreDependencies ++ coreTestDependencies))

lazy val spark = project.in(file("spark"))
  .settings(commonSettings: _*)
  .settings(name := "tranquility-spark")
  .settings(resolvers += "clojars" at "http://clojars.org/repo/")
  .settings(libraryDependencies ++= sparkDependencies)
  .dependsOn(core % "test->test;compile->compile")

lazy val storm = project.in(file("storm"))
  .settings(commonSettings: _*)
  .settings(name := "tranquility-storm")
  .settings(resolvers += "clojars" at "http://clojars.org/repo/")
  .settings(libraryDependencies ++= stormDependencies)
  .dependsOn(core % "test->test;compile->compile")

lazy val samza = project.in(file("samza"))
  .settings(commonSettings: _*)
  .settings(name := "tranquility-samza")
  .settings(libraryDependencies ++= (samzaDependencies ++ samzaTestDependencies))
  // don't compile or publish for Scala > 2.10
  .settings((skip in compile) := scalaVersion { sv => ! sv.startsWith("2.10.") }.value)
  .settings((skip in test) := scalaVersion { sv => !sv.startsWith("2.10.") }.value)
  .settings(publishArtifact <<= scalaVersion { sv => sv.startsWith("2.10.") })
  .settings(publishArtifact in Test := false)
  .dependsOn(core % "test->test;compile->compile")
