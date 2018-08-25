inThisBuild(
  List(
    organization := "com.nequissimus",
    homepage := Some(url("http://nequissimus.com/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "NeQuissimus",
        "Tim Steinbach",
        "steinbach.tim@gmail.com",
        url("http://nequissimus.com/")
      )
    )
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .aggregate(module1, module2)

lazy val module1 = project
  .in(file("./module1"))
  .settings(
    name := "module1",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.2.26",
      "org.specs2" %% "specs2-core" % "4.3.4" % "test"
    ),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    crossScalaVersions := List("2.12.6", "2.11.12")
  )

lazy val module2 = project
  .in(file("./module2"))
  .settings(
    name := "module2",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.2.26",
      "org.specs2" %% "specs2-core" % "4.3.4" % "test"
    ),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    crossScalaVersions := List("2.12.6", "2.11.12")
  )
