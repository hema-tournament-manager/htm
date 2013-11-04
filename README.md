# Hema Tournament Manager

## Components

1. HTM-Admin
2. HTM-Battle
3. HTM-Viewer-JME
4. HTM-Lib

## Development environment

1. [Eclipse IDE for Java EE Developers](http://eclipse.org/downloads)
2. [Scala IDE for Scala 2.10](http://scala-ide.org/download/current.html) (with Play plugin)
3. [SBT](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)

## Getting started

* Clone this repository
* Run `sbt eclipse` to generate Eclipse project files for all the components
* Import all four projects into Eclipse
* In Eclipse, run src/test/scala/Run<Admin|BattleStation|JMonkeyViewer>.scala to start an application
* Open application in a browser using localhost:<port> where port is 8079 for Admin, 8080 for Battle and 8081 for Viewer

## OSX

* To get sbt admin to work under OSX you will need to run the following code before starting the admin thread.
export SBT_OPTS=-XX:MaxPermSize=256m
