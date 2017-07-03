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
        "com.github.scopt" %%% "scopt"     % "3.6.0"))

lazy val disableDocs: Seq[Setting[_]] =
  Seq(
      sources in doc in Compile := List())

lazy val bindgen =
  project.in(file("bindgen"))
    .enablePlugins(ScalaNativePlugin)
    .settings(platform)
//    .settings(disableDocs)
//    .settings(
//      nativeVerbose := true,
//      nativeClangOptions ++= Seq("-O2"))
