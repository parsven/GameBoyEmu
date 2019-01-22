

name := "GameBoyEmuRoot"

scalaVersion in ThisBuild := "2.12.3"


lazy val root = project.in(file(".")).
  aggregate(gbeJS, gbeJVM).
  settings(
    publish := {},
    publishLocal := {}
  ).enablePlugins(ScalaJSPlugin)


lazy val gameboyEmu = crossProject.in(file(".")).
  settings(
    name := "GameBoyEmu"
     , version := "0.1"

  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.intellij" % "forms_rt" % "6.0.3",
      "com.jgoodies" % "jgoodies-forms" % "1.6.0",
      "com.jgoodies" %  "jgoodies-common" % "1.6.0",
      "org.mockito" % "mockito-all" % "1.9.0" % "test"
    )
  ).
  jsSettings(
    // Add JS-specific settings here
  )

lazy val gbeJVM = gameboyEmu.jvm
lazy val gbeJS = gameboyEmu.js



mainClass in  (Compile, run) := Some("gameboyemu.swingui.DebuggerForm")

scalaJSUseMainModuleInitializer := true

