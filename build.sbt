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
      |import kantan.bson.generic._
      |import kantan.bson.ops._
    """.stripMargin
  )
  .aggregate(bson, generic)
  .dependsOn(bson, generic)



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


lazy val generic = project
  .settings(
    moduleName := "kantan.bson-generic",
    name       := "generic"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(bson)
  .settings(libraryDependencies ++= Seq(
    "com.nrinaudo"  %% "kantan.codecs-shapeless"      % Versions.kantanCodecs,
    "org.scalatest" %% "scalatest"                    % Versions.scalatest    % "test"
  ))
