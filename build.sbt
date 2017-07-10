import scalanative.sbtplugin.ScalaNativePluginInternal._

organization := "org.scala-native"
name := "scala-bindgen"

lazy val platform: Seq[Setting[_]] =
  Seq(
    scalaVersion := "2.11.11",
    libraryDependencies ++=
      Seq(
        "org.scala-native" %%% "nativelib" % "0.3.1",
        "org.scala-native" %%% "javalib"   % "0.3.1",
        "org.scala-native" %%% "scalalib"  % "0.3.1",
        "com.github.scopt" %%% "scopt"     % "3.6.0"
      )
  )

lazy val testSettings: Seq[Setting[_]] =
  Seq(
    fork in Test := true,
    libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.8" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

lazy val disableDocs: Seq[Setting[_]] =
  Seq(sources in doc in Compile := List())

lazy val bindgen =
  project
    .in(file("bindgen"))
    .enablePlugins(ScalaNativePlugin)
    .settings(platform)
    .settings(testSettings)
    .settings(
      nativeCompileLL in Compile += {
        val compiler = (nativeClang in Compile).value.getAbsolutePath
        val opts     = (nativeCompileOptions in Compile).value
        val cpath    = (resourceDirectory in Compile).value / "clang.c"
        val opath    = (crossTarget in Compile).value / "clang.o"
        val compilec = Seq(compiler) ++ opts ++ Seq("-c",
                                                    cpath.toString,
                                                    "-o",
                                                    opath.toString)

        streams.value.log.info(s"Compiling $cpath to $opath")
        val exitCode = Process(compilec, target.value) ! streams.value.log
        if (exitCode != 0) {
          streams.value.log.error("Failed to compile " + cpath)
        }
        opath
      }
    )
    .settings(
      javaOptions in Test += "-Dnative.bin=" + (nativeLinkLL in Compile).value
    )

lazy val tests =
  project
    .in(file("tests"))
    .settings(testSettings)
    .settings(
      javaOptions in Test += "-Dnative.bin=" + nativeLinkLL.in(bindgen, Compile).value
    )
