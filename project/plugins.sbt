// xsbt-web-plugin
resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"  

// sbteclipse
resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.0")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")