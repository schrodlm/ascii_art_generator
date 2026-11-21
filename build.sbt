import org.scalajs.linker.interface.OutputPatterns

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version := "1.0.0"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature"),
  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.20" % Test
)

lazy val root = project
  .in(file("."))
  .aggregate(core.jvm, core.js, cli, js)
  .settings(name := "ascii-art")

// The platform-free image pipeline, compiled for both the JVM and the browser.
lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)

lazy val cli = project
  .dependsOn(core.jvm)
  .settings(commonSettings)

// The JS library: an ES module that other JavaScript imports.
lazy val js = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core.js)
  .settings(
    commonSettings,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.1",
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
    }
  )
