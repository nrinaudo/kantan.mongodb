kantanProject in ThisBuild := "mongodb"


// - root projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val root = Project(id = "kantan-mongodb", base = file("."))
  .settings(moduleName := "root")
  .enablePlugins(UnpublishedPlugin)
  .settings(
    initialCommands in console :=
    """
      |import kantan.bson._
      |import kantan.bson.ops._
    """.stripMargin
  )
  .aggregate(bson)
  .dependsOn(bson)



// - bson --------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val bson = project
  .settings(
    moduleName := "kantan.bson",
    name       := "bson"
  )
  .enablePlugins(PublishedPlugin, spray.boilerplate.BoilerplatePlugin)
  .settings(libraryDependencies ++= Seq(
    "com.nrinaudo"  %% "kantan.codecs"      % Versions.kantanCodecs,
    "org.mongodb"   %  "bson"               % Versions.mongodb,
    "org.scalatest" %% "scalatest"          % Versions.scalatest % "test",
    "com.nrinaudo"  %% "kantan.codecs-laws" % Versions.kantanCodecs % "test"
  ))
