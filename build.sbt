
name := "GameBoyEmu"

version := "0.1"

scalaVersion := "2.12.4"

mainClass in  (Compile, run) := Some("gameboyemu.swingui.DebuggerForm")

libraryDependencies ++= Seq(
  "com.intellij" % "forms_rt" % "6.0.3",
  "com.jgoodies" % "jgoodies-forms" % "1.6.0",
  "com.jgoodies" %  "jgoodies-common" % "1.6.0"
)
