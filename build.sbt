name := "scikit"

version := "1.0"

scalaVersion := "2.9.1"

// search subdirectories recursively for unmanaged libraries
unmanagedJars in Compile <++= unmanagedBase map { ub =>
  (ub ** "*.jar").classpath
}
