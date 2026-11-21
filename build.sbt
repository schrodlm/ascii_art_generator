import org.scalajs.linker.interface.OutputPatterns
import scala.sys.process.Process

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
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
    },
    // The library is tested from JavaScript, as its users call it: node runs the tests in
    // src/test/js against both the development and the production build, linked first.
    // Then TypeScript checks the hand-written type declarations.
    Test / test := {
      val _ = (Test / test).value  // the Scala tests first, should the module ever get any
      val log = streams.value.log
      val tests = (baseDirectory.value / "src" / "test" / "js" ** "*.test.mjs").get.map(_.getPath)
      // Asking for each link's result links it first, and names the file it wrote.
      val libraries = Seq(
        (Compile / fastLinkJSOutput).value / (Compile / fastLinkJS).value.data.publicModules.head.jsFileName,
        (Compile / fullLinkJSOutput).value / (Compile / fullLinkJS).value.data.publicModules.head.jsFileName
      )
      for (library <- libraries) {
        log.info(s"Testing $library")
        val exit = Process("node" +: "--test" +: tests, None, "ASCII_ART_JS" -> library.getPath).!
        if (exit != 0) throw new MessageOnlyException(s"JavaScript tests failed against $library")
      }

      // TypeScript checks package/test/usage.ts against the hand-written package/index.d.ts.
      val npmPackage = baseDirectory.value / "package"
      val tsc = npmPackage / "node_modules" / ".bin" / "tsc"
      if (!tsc.exists) throw new MessageOnlyException(s"TypeScript is not installed: run npm ci in $npmPackage")
      log.info("Checking index.d.ts with TypeScript")
      if (Process(Seq(tsc.getPath, "-p", npmPackage.getPath)).! != 0) {
        throw new MessageOnlyException("index.d.ts does not match test/usage.ts")
      }
    }
  )
