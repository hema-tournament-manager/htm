import sbt._
import sbt.Keys._
import com.earldouglas.xsbtwebplugin.WebPlugin._
import com.typesafe.sbt.SbtScalariform._

object HtmBuild extends Build {
	import Dependencies._
	import BuildSettings._
	
	lazy val adminSettings = buildSettings ++ webSettings ++ Format.settings ++ Seq(
		name := buildName + "-Admin",
		libraryDependencies ++= Seq(jettyWebapp, jettyTest, liftWebkit, liftMapper, junit))
		
	lazy val libSettings = buildSettings ++ Format.settings ++ Seq(
		name := buildName + "-Lib",
		libraryDependencies ++= Seq(liftWebkit, liftMapper, dispatch))
		
	lazy val admin = Project(
		id = "admin",
		base = file("htm-admin"),
		settings = adminSettings) dependsOn (lib)
		
	lazy val lib = Project(
		id = "lib",
		base = file("htm-lib"),
		settings = libSettings)
}

object BuildSettings {
  val buildOrganization = "nl.malienkolders"
  val buildName = "HTM"
  val buildVersion = "0.0.1-SNAPSHOT"
  val buildScalaVersion = "2.10.2"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "UTF-8"),
    resolvers := Seq("Vaadin Snapshots" at "https://oss.sonatype.org/content/repositories/vaadin-snapshots/",
      "Scala Tools Releases" at "http://scala-tools.org/repo-releases/",
      "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
      "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
      "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"),
    autoScalaLibrary := true,
    offline := false)
}

object Format {
  lazy val settings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := formattingPreferences
  )
 
  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences().
      setPreference(AlignParameters, true).
      setPreference(DoubleIndentClassDeclaration, true)
  }
}

object Dependencies {
  val liftVersion = "2.5"
  val jettyVersion = "8.1.12.v20130726"
  val scalaTestVersion = "2.0.M5-B1"
  val junitVersion = "4.11"
  val mockitoVersion = "1.9.0"

  val scala = "org.scala-lang" % "scala-library" % BuildSettings.buildScalaVersion % "provided"
  val scalaReflect = "org.scala-lang" % "scala-reflect" % BuildSettings.buildScalaVersion
  val scalaActors = "org.scala-lang" % "scala-actors" % BuildSettings.buildScalaVersion % "test"
  val servletApi = "javax.servlet" % "servlet-api" % "2.4"
  val portletApi = "javax.portlet" % "portlet-api" % "2.0"
  val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"
  val jettyTest = "org.mortbay.jetty" % "jetty" % "6.1.26" % "test"
  val scalaTest = "org.scalatest" % "scalatest_2.10.0-RC5" % scalaTestVersion % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.7" % "test->default"
  val mockito = "org.mockito" % "mockito-all" % mockitoVersion % "test"
  val slf4j = "org.slf4j" % "slf4j-nop" % "1.6.4"
  val h2 = "com.h2database" % "h2" % "1.3.171"
  val liftWebkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"
  val liftMapper = "net.liftweb" %% "lift-mapper" % liftVersion % "compile"
  val dispatch = "net.databinder.dispatch" %% "core" % "0.9.1"
  val junit = "junit" % "junit" % junitVersion
}