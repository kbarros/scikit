name := "scikit"

version := "1.0"

scalaVersion := "2.10.3"

// search subdirectories recursively for unmanaged libraries
unmanagedJars in Compile <++= unmanagedBase map { ub =>
  (ub ** "*.jar").classpath
}

// install platform specific JOGL
libraryDependencies ++= {
  val os = sys.props("os.name") match {
    case "Linux" => "linux"
    case "Mac OS X" => "macosx"
    case os if os.startsWith("Windows") => "windows"
    case os => sys.error("Cannot obtain lib for OS: " + os)
  }
  val arch = if (os == "macosx") "universal" else sys.props("os.arch") match {
    case "amd64" => "amd64"
    case "i386" => "i586"
    case "x86" => "i586"
    case arch => sys.error("Cannot obtain lib for arch: " + arch)
  }
  val version = "2.1.4"
  val jogl = "org.jogamp.jogl"    % "jogl-all"   % version classifier "natives-"+os+"-"+arch classifier ""
  val glue = "org.jogamp.gluegen" % "gluegen-rt" % version classifier "natives-"+os+"-"+arch classifier ""
  Seq(jogl, glue)
}
