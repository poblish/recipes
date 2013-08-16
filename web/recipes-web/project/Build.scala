import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "recipes-web"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    "uk.co.recipes" % "recipes-domain" % "0.0.1-SNAPSHOT",
    "com.squareup.dagger" % "dagger-compiler" % "1.1.0",
    "com.codahale.metrics" % "metrics-core" % "3.0.1",
    "com.codahale.metrics" % "metrics-servlets" % "3.0.1",
    "com.feth" %% "play-authenticate" % "0.3.0-SNAPSHOT"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",

    // See: https://github.com/joscha/play-authenticate/blob/master/samples/java/Getting%20Started.md

    resolvers += "play-easymail (release)" at "http://joscha.github.com/play-easymail/repo/releases/",
    resolvers += "play-easymail (snapshot)" at "http://joscha.github.com/play-easymail/repo/snapshots/",
    resolvers += "play-authenticate (release)" at "http://joscha.github.com/play-authenticate/repo/releases/",
    resolvers += "play-authenticate (snapshot)" at "http://joscha.github.com/play-authenticate/repo/snapshots/"
  )
}
