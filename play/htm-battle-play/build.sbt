name := "HTM-Battle-Play"

organization := "nl.htm"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "jquery" % "1.10.2-1",
  "org.webjars" % "bootstrap" % "3.0.0" exclude ("org.webjars", "jquery"),
  "org.webjars" % "angularjs" % "1.1.5" exclude ("org.webjars", "jquery"),
  "org.webjars" % "underscorejs" % "1.5.1" exclude ("org.webjars", "jquery")  
)     

play.Project.playScalaSettings
