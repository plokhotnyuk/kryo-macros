import sbt.Keys.scalacOptions
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import scala.sys.process._

lazy val oldVersion = "git describe --abbrev=0".!!.trim.replaceAll("^v", "")

def mimaSettings = mimaDefaultSettings ++ Seq(
  mimaCheckDirection := {
    def isPatch = {
      val Array(newMajor, newMinor, _) = version.value.split('.')
      val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
      newMajor == oldMajor && newMinor == oldMinor
    }

    if (isPatch) "both" else "backward"
  },
  mimaPreviousArtifacts := {
    def isCheckingRequired = {
      val Array(newMajor, newMinor, _) = version.value.split('.')
      val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
      newMajor == oldMajor && (newMajor != "0" || newMinor == oldMinor)
    }

    if (isCheckingRequired) Set(organization.value %% moduleName.value % oldVersion)
    else Set()
  }
)

lazy val kryo = project.in(file("."))
  .settings(
    crossScalaVersions := Seq("2.12.4", "2.11.12"),
    releaseCrossBuild := true,
    publish := (),
    inThisBuild(Seq(
      organization := "com.evolutiongaming",
      scalaVersion := "2.12.4",
      startYear := Some(2016),
      organizationName := "Evolution Gaming",
      organizationHomepage := Some(url("https://www.evolutiongaming.com/")),
      bintrayOrganization := Some("evolutiongaming"),
      resolvers += Resolver.bintrayRepo("evolutiongaming", "maven"),
      homepage := Some(url("https://github.com/evolution-gaming/kryo-macros")),
      licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
      scalacOptions ++= Seq(
        "-encoding", "UTF-8",
        "-feature",
        "-unchecked",
        "-deprecation",
        "-Xlint",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-Xfuture",
        "-Xmacro-settings:print-serializers"
      )
    ))
  ).aggregate(macros, benchmark)

lazy val macros = project
  .settings(mimaSettings: _*)
  .settings(
    name := "kryo-macros",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.esotericsoftware" % "kryo" % "4.0.1",
      "joda-time" % "joda-time" % "2.8",
      "org.joda" % "joda-convert" % "1.7",
      "org.scalatest" %% "scalatest" % "3.0.3" % Test
    )
  )

lazy val benchmark = project
  .enablePlugins(JmhPlugin)
  .settings(
    name := "kryo-benchmark",
    publish := (),
    libraryDependencies ++= Seq(
      "pl.project13.scala" % "sbt-jmh-extras" % "0.3.2",
      "org.scalatest" %% "scalatest" % "3.0.3" % Test
    )
  ).dependsOn(macros)
