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

## Installing under CentOS 6

* Prerequisites (run as root or sudo):
  - SBT (http://scalasbt.artifactoryonline.com/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.0/sbt.rpm)
      yum localinstall sbt.rpm
  - JRE (http://www.java.com/getjava/ -> jre-7u45-linux-x64.rpm)
      yum localinstall jre-7u45-linux-x64.rpm
  - Scala (http://www.scala-lang.org/files/archive/scala-2.10.3.tgz)
      tar xvf scala-2.10.3.tgz
      mv scala-2.10.3 /usr/lib
      ln -s /usr/lib/scala-2.10.3 /usr/lib/scala
      echo 'export PATH=$PATH:/usr/lib/scala/bin' > /etc/profile/scala.sh
      echo 'export SBT_OPTS=-XX:MaxPermSize=256m' >> /etc/profile/scala.sh

