import ReleaseTransformations._
import sbt.Keys._

name := "commercetools-sunrise-cms"

organization := "io.commercetools.sunrise"

/**
 * PROJECT DEFINITIONS
 */

lazy val `commercetools-sunrise-cms` = (project in file("."))
  .aggregate(`cms-common`, `cms-contentful`)
  .settings(javaUnidocSettings ++ commonSettings : _*)

lazy val `cms-common` = project
  .configs(IntegrationTest)
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
//  .dependsOn(`cms-common`)

lazy val `cms-contentful` = project
  .configs(IntegrationTest)
  .settings(commonSettings ++ commonTestSettings : _*)
  .dependsOn(`cms-common`)


/**
 * COMMON SETTINGS
 */

lazy val commonSettings = releaseSettings ++ Seq (
  scalaVersion := "2.11.8",
  javacOptions in (Compile, doc) := Seq("-quiet", "-notimestamp"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)


/**
 * TEST SETTINGS
 */

lazy val commonTestSettings = itBaseTestSettings ++ configCommonTestSettings("test,it")

lazy val itBaseTestSettings = Defaults.itSettings ++ configTestDirs(IntegrationTest, "it")

def configTestDirs(config: Configuration, folderName: String) = Seq(
  javaSource in config := baseDirectory.value / folderName,
  scalaSource in config := baseDirectory.value / folderName,
  resourceDirectory in config := baseDirectory.value / s"$folderName/resources"
)

def configCommonTestSettings(scopes: String) = Seq(
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
  libraryDependencies ++= Seq (
    "com.novocode" % "junit-interface" % "0.11" % scopes,
    "org.assertj" % "assertj-core" % "3.4.1" % scopes
  )
)

/**
 * RELEASE SETTINGS
 */

lazy val releaseSettings = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)
