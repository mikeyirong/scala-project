name := "wish-product-crawler"

organization := "com.tvc.bigdata"

version := "1.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

javacOptions ++= Seq("-encoding", "UTF-8")

libraryDependencies ++= Seq(
  "com.tvc.bigdata" % "tcrawler_2.11" % "1.0.0-SNAPSHOT",
  "com.tvc.be" % "dbwrapper" % "1.0.0",
  "org.jsoup" % "jsoup" % "1.9.1",
  "mysql" % "mysql-connector-java" % "5.1.34",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.5-M1",
  "com.alibaba" % "fastjson" % "1.2.24"
)


resolvers ++= Seq(
     "tvc Repository" at "http://nexus.mindcenter.cn:12580/nexus/content/groups/public/"
)


EclipseKeys.withSource := true

publishMavenStyle := true



publishTo <<= version { v: String =>
  val nexus = "http://nexus.mindcenter.cn:12580/nexus" 
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "/content/repositories/snapshots/")
  else
    Some("releases" at nexus + "/content/repositories/releases/")
}