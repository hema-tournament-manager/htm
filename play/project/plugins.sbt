// sbteclipse
resolvers += Classpaths.typesafeResolver

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")
