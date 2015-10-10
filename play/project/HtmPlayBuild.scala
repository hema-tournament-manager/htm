import sbt._
import Keys._
import play.Project._
import com.typesafe.sbt.SbtScalariform._

object HtmBuild extends Build {
	def appName(name: String) = s"htm-${name}-play"
	val appVersion = "1.4.10"
	
	lazy val appDependencies = Seq(
		"org.webjars" %% "webjars-play" % "2.2.0",
		"org.webjars" % "jquery" % "1.10.2-1",
		"org.webjars" % "jquery-ui" % "1.10.3",
		"org.webjars" % "bootstrap" % "3.0.0" exclude ("org.webjars", "jquery"),
		"org.webjars" % "angularjs" % "1.1.5" exclude ("org.webjars", "jquery"),
		"org.webjars" % "angular-ui-bootstrap" % "0.13.0" exclude ("org.webjars", "jquery"),
		"org.webjars" % "underscorejs" % "1.5.1" exclude ("org.webjars", "jquery"),
		"nl.malienkolders" %% "htm-lib" % appVersion,
		"net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
		"net.liftweb" %% "lift-webkit" % "2.5.1",
		"commons-io" % "commons-io" % "2.4")

	val viewer = play.Project(
		appName("viewer"),
		appVersion,
		appDependencies,
		path = file("htm-viewer-play")).settings(Format.settings: _*).settings(defaultScalaSettings: _*).
    	settings(closureCompilerOptions := Seq("rjs"))
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
