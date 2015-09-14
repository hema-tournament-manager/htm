import sbt._
import sbt.Keys._
import com.earldouglas.xsbtwebplugin.WebPlugin._
import com.typesafe.sbt.SbtScalariform._
import com.earldouglas.xsbtwebplugin.PluginKeys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object HtmBuild extends Build {
	import Dependencies._
	import BuildSettings._
	
	lazy val adminSettings = buildSettings ++ webSettings ++ Format.settings ++ Seq(
		name := buildName + "-Admin",
		port in container.Configuration := 8079,
		libraryDependencies ++= Seq(
               "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container",
               "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
               "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
               "net.liftweb" %% "lift-mapper" % liftVersion % "compile",
               //"net.liftweb" %% "lift-textile" % liftVersion % "compile",
               "org.mortbay.jetty" % "jetty" % "6.1.26" % "test",
               "junit" % "junit" % "4.7" % "test",
               "ch.qos.logback" % "logback-classic" % "0.9.26",
               "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
               "com.h2database" % "h2" % "1.2.147",
               "net.databinder.dispatch" %% "dispatch-core" % dispatchVersion,
							 "org.apache.poi" % "poi" % "3.9",
							 "org.apache.poi" % "poi-ooxml" % "3.9",
		"commons-io" % "commons-io" % "2.4"
))
		
	lazy val libSettings = buildSettings ++ Format.settings ++ Seq(
		name := buildName + "-Lib",
		libraryDependencies ++= Seq(
		       "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
               "net.liftweb" %% "lift-mapper" % liftVersion % "compile",
               //"net.liftweb" %% "lift-textile" % liftVersion % "compile",
               "org.mortbay.jetty" % "jetty" % "6.1.26" % "test",
               "junit" % "junit" % "4.7" % "test",
               "ch.qos.logback" % "logback-classic" % "0.9.26",
               "org.specs2" %% "specs2" % "2.2.3" % "test",
               "com.h2database" % "h2" % "1.2.147",
               "net.databinder.dispatch" %% "dispatch-core" % dispatchVersion,
               "com.github.nscala-time" %% "nscala-time" % "0.6.0",
		"com.google.zxing" % "core" % "2.3.0",
		"com.google.zxing" % "javase" % "2.3.0"))
		
	lazy val importerSettings = buildSettings ++ Format.settings ++ Seq(
		name := buildName + "-Importer",
		libraryDependencies ++= Seq(
			"org.specs2" %% "specs2" % "2.2.3" % "test",
			"org.apache.poi" % "poi-ooxml" % "3.9"))

	lazy val admin = Project(
		id = "admin",
		base = file("htm-admin"),
		settings = adminSettings) dependsOn (lib, importer)
		
	lazy val lib = Project(
		id = "lib",
		base = file("htm-lib"),
		settings = libSettings)

	lazy val importer = Project(
		id = "importer",
		base = file("htm-importer"),
		settings = importerSettings)
}

object BuildSettings {
  val buildOrganization = "nl.malienkolders"

  val buildName = "HTM"
  val buildVersion = "1.4.2"
  val buildScalaVersion = "2.10.4"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "UTF-8"),
    resolvers := Seq("Vaadin Snapshots" at "https://oss.sonatype.org/content/repositories/vaadin-snapshots/",
      "Scala Tools Releases" at "http://scala-tools.org/repo-releases/",
      "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
      "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
      "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/",
      "Sonatype OSS Public" at "https://oss.sonatype.org/content/groups/public/"),
    autoScalaLibrary := true,
    offline := false,
    javaOptions := Seq("-Djava.awt.headless=true"),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
	EclipseKeys.withSource := true)
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
  val liftVersion = "2.5.1"
  val jettyVersion = "8.0.4.v20111024" //"8.1.12.v20130726"
  val scalaTestVersion = "2.0.M5-B1"
  val junitVersion = "4.7" //"4.11"
  val mockitoVersion = "1.9.0"
  val dispatchVersion = "0.11.0"

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
  val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchVersion
  val junit = "junit" % "junit" % junitVersion
}
