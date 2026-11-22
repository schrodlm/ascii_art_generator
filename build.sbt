import org.scalajs.linker.interface.OutputPatterns
import scala.sys.process.Process

ThisBuild / scalaVersion := "2.13.18"

// What Maven Central shows about the library. The version comes from the git tag being released
// (v0.1.0 publishes 0.1.0), which sbt-ci-release reads.
ThisBuild / organization := "io.github.schrodlm"
ThisBuild / homepage := Some(url("https://github.com/schrodlm/clascii"))
ThisBuild / licenses := List(License.MIT)
ThisBuild / developers := List(
  Developer("schrodlm", "Matěj Schrödl", "matej.schrodl@gmail.com", url("https://github.com/schrodlm"))
)
ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/schrodlm/clascii"),
  "scm:git:https://github.com/schrodlm/clascii.git"
))

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-deprecation", "-feature"),
  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.20" % Test
)

lazy val root = project
  .in(file("."))
  .aggregate(core.jvm, core.js, cli, js)
  .settings(name := "clascii-root", publish / skip := true)

// The platform-free image pipeline, compiled for both the JVM and the browser. The one module
// published to Maven Central, once for each.
lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    commonSettings,
    name := "clascii",
    description := "Turns pictures into ASCII art, with tables of characters or braille."
  )

lazy val cli = project
  .dependsOn(core.jvm)
  .settings(commonSettings, publish / skip := true)

lazy val npmPackage = taskKey[File]("Puts the minified release build and the licence into js/package, ready for npm")
lazy val demo = taskKey[File]("Puts the minified release build next to the demo page, so js/demo is a static site")

// The JS library: an ES module that other JavaScript imports.
lazy val js = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core.js)
  .settings(
    commonSettings,
    publish / skip := true,  // published to npm instead, by way of npmPackage
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
    },
    // The npm package is js/package: the hand-written index.d.ts and package.json, plus the
    // release build, minified, and the licence, both copied in here.
    npmPackage := {
      val log = streams.value.log
      val dir = baseDirectory.value / "package"
      // Asking for the link's result links it first, and names the file it wrote.
      val release = (Compile / fullLinkJSOutput).value / (Compile / fullLinkJS).value.data.publicModules.head.jsFileName
      val esbuild = dir / "node_modules" / ".bin" / "esbuild"
      if (!esbuild.exists) throw new MessageOnlyException(s"esbuild is not installed: run npm ci in $dir")
      // Scala.js shortens only some names; esbuild shortens the rest and drops the whitespace.
      val bundle = dir / "main.mjs"
      val minify = Seq(esbuild.getPath, release.getPath, "--minify", "--format=esm", "--log-level=warning", s"--outfile=$bundle")
      if (Process(minify).! != 0) throw new MessageOnlyException("esbuild could not minify the release build")
      IO.copyFile((LocalRootProject / baseDirectory).value / "LICENSE", dir / "LICENSE")
      log.info(s"Packaged $bundle, ${bundle.length / 1024} KB")
      dir
    },
    // The demo page loads ./main.mjs, the same bundle npm ships, so the folder can be served as it is.
    demo := {
      val dir = baseDirectory.value / "demo"
      IO.copyFile(npmPackage.value / "main.mjs", dir / "main.mjs")
      streams.value.log.info(s"The demo is ready in $dir")
      dir
    },
    // The library is tested from JavaScript, as its users call it: node runs the tests in
    // src/test/js against the development build and against the minified build npm would ship.
    // Then TypeScript checks the hand-written type declarations.
    Test / test := {
      val _ = (Test / test).value  // the Scala tests first, should the module ever get any
      val log = streams.value.log
      val tests = (baseDirectory.value / "src" / "test" / "js" ** "*.test.mjs").get.map(_.getPath)
      val packageDir = npmPackage.value
      val libraries = Seq(
        (Compile / fastLinkJSOutput).value / (Compile / fastLinkJS).value.data.publicModules.head.jsFileName,
        packageDir / "main.mjs"
      )
      for (library <- libraries) {
        log.info(s"Testing $library")
        val exit = Process("node" +: "--test" +: tests, None, "ASCII_ART_JS" -> library.getPath).!
        if (exit != 0) throw new MessageOnlyException(s"JavaScript tests failed against $library")
      }

      // The package as npm would install it: imported by name, and holding only what it should.
      log.info(s"Testing the package in $packageDir")
      val packageTests = (packageDir / "test" ** "*.test.mjs").get.map(_.getPath)
      if (Process("node" +: "--test" +: packageTests, packageDir).! != 0) {
        throw new MessageOnlyException(s"The package in $packageDir does not work as installed")
      }

      // TypeScript checks package/test/usage.ts against the hand-written package/index.d.ts.
      val tsc = packageDir / "node_modules" / ".bin" / "tsc"
      if (!tsc.exists) throw new MessageOnlyException(s"TypeScript is not installed: run npm ci in $packageDir")
      log.info("Checking index.d.ts with TypeScript")
      if (Process(Seq(tsc.getPath, "-p", packageDir.getPath)).! != 0) {
        throw new MessageOnlyException("index.d.ts does not match test/usage.ts")
      }
    }
  )
