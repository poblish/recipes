name         := "recipes-web"

version      := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.11"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

libraryDependencies += "uk.co.recipes" % "recipes-domain" % "0.0.1-SNAPSHOT" // exclude("org.scala-stm", "scala-stm_2.10.0")
libraryDependencies += "com.squareup.dagger" % "dagger-compiler" % "1.1.0"
libraryDependencies += "com.codahale.metrics" % "metrics-core" % "3.0.1"
libraryDependencies += "com.codahale.metrics" % "metrics-servlets" % "3.0.1"
libraryDependencies += "com.feth" %% "play-authenticate" % "0.8.3"  // "0.5.0-SNAPSHOT" // exclude("org.scala-stm", "scala-stm_2.10.0")

resolvers += Resolver.mavenLocal

// FIXME @ https://www.playframework.com/documentation/2.6.x/JavaDependencyInjection#Static-routes-generator
routesGenerator := StaticRoutesGenerator

//resolvers += "play-easymail (release)" at "http://joscha.github.com/play-easymail/repo/releases/"
//resolvers += "play-easymail (snapshot)" at "http://joscha.github.com/play-easymail/repo/snapshots/"
//resolvers += "play-authenticate (release)" at "http://joscha.github.com/play-authenticate/repo/releases/"
//resolvers += "play-authenticate (snapshot)" at "http://joscha.github.com/play-authenticate/repo/snapshots/"

//object ApplicationBuild extends Build {
//
//  val scalaVersion    = "2.10.2"
//
//  val appName         = "recipes-web"
//  val appVersion      = "1.0-SNAPSHOT"
//
//  val appDependencies = Seq(
//    // Add your project dependencies here,
//    javaCore, filters,
//
//    "uk.co.recipes" % "recipes-domain" % "0.0.1-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0"),
//    "com.squareup.dagger" % "dagger-compiler" % "1.1.0",
//    "com.codahale.metrics" % "metrics-core" % "3.0.1",
//    "com.codahale.metrics" % "metrics-servlets" % "3.0.1",
//    "com.feth" %% "play-authenticate" % "0.5.0-SNAPSHOT" exclude("org.scala-stm", "scala-stm_2.10.0")
//  )
//
//  val main = Project(appName, file(".")).enablePlugins(play.PlayJava).settings(
//    resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
//
//    // See: https://github.com/joscha/play-authenticate/blob/master/samples/java/Getting%20Started.md
//
//    resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.com/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
//    resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.com/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),
//    resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.com/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns),
//    resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.com/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns)
//  )
//}
