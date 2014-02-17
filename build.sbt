name := "scikit"

version := "1.0"

scalaVersion := "2.10.3"

// search subdirectories recursively for unmanaged libraries
unmanagedJars in Compile <++= unmanagedBase map { ub =>
  (ub ** "*.jar").classpath
}
