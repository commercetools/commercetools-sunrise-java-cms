import ReleaseTransformations._
import sbt.Keys._

name := "commercetools-sunrise-cms"

organization in ThisBuild := "com.commercetools.sunrise.cms"

/**
 * PROJECT DEFINITIONS
 */

lazy val `commercetools-sunrise-cms` = (project in file("."))
  .aggregate(`cms-api`, `cms-contentful`)
  .settings(javaUnidocSettings ++ commonSettings : _*)

lazy val `cms-api` = project
  .settings(commonSettings ++ commonTestSettings : _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.4",
      "com.google.code.findbugs" % "jsr305" % "3.0.0"
    )
  )

//lazy val `cms-file-based` = project
//  .configs(IntegrationTest)
//  .settings(commonSettings ++ commonTestSettings : _*)
//  .dependsOn(`cms-api`)

lazy val `cms-contentful` = project
  .settings(commonSettings ++ commonTestSettings : _*)
  .settings(
    libraryDependencies ++= Seq(
      // okhttp is used by contentful as optional dependency as HTTP client
      "com.squareup.okhttp3" % "okhttp" % "3.2.0",
      "com.contentful.java" % "java-sdk" % "7.0.2"
    )
  )
  .dependsOn(`cms-api`)


/**
 * COMMON SETTINGS
 */

lazy val commonSettings = Release.publishSettings ++ Seq (
  autoScalaLibrary := false, //this is a pure Java module, no Scala dependency
  crossPaths := false, //this is a pure Java module, no Scala version suffix on JARs
  javacOptions in (Compile, doc) := Seq("-quiet", "-notimestamp"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)


/**
 * TEST SETTINGS
 */

lazy val commonTestSettings = configCommonTestSettings("test")

def configTestDirs(config: Configuration, folderName: String) = Seq(
  javaSource in config := baseDirectory.value / folderName,
  scalaSource in config := baseDirectory.value / folderName,
  resourceDirectory in config := baseDirectory.value / s"$folderName/resources"
)

def configCommonTestSettings(scopes: String) = Seq(
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
  libraryDependencies ++= Seq (
    "com.novocode" % "junit-interface" % "0.11" % scopes,
    "org.assertj" % "assertj-core" % "3.4.1" % scopes,
    "org.mockito" % "mockito-all" % "1.10.19" % scopes
  )
)

/**
 * RELEASE SETTINGS
 */

publishMavenStyle in ThisBuild := true

publishArtifact in Test in ThisBuild := false

publishTo in ThisBuild <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}