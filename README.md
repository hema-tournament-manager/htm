# Hema Tournament Manager

## Components

1. HTM-Admin
2. HTM-Battle
3. HTM-Viewer-JME
4. HTM-Lib

## Development environment

1. [Eclipse IDE for Java EE Developers](http://eclipse.org/downloads/packages/release/juno/sr2) (Juno or previous, Kepler doesn't seem to work)
2. [Scala IDE for Scala 2.9](http://scala-ide.org/download/current.html) (with Play plugin)
3. [SBT 0.12.2](http://www.scala-sbt.org/0.12.2/docs/Getting-Started/Setup.html)

## Getting started

* Clone this repository
* Run `sbt eclipse` to generate Eclipse project files for all the components
* Import all four projects into Eclipse
* In Eclipse, run src/test/scala/Run<Admin|BattleStation|JMonkeyViewer>.scala to start an application
* Open application in a browser using localhost:<port> where port is 8079 for Admin, 8080 for Battle and 8081 for Viewer
