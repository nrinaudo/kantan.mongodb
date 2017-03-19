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
      |import kantan.bson.generic._
    """.stripMargin
  )
  .aggregate(mongodb, bson, generic, jodaTime, laws)
  .aggregateIf(java8Supported)(java8)
  .dependsOn(mongodb, generic)



// - bson --------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val mongodb = project
  .settings(
    moduleName := "kantan.mongodb",
    name       := "mongodb"
  )
  .enablePlugins(PublishedPlugin, BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](version, BuildInfoKey.action("commit") {git.gitHeadCommit.value}),
    buildInfoPackage := "kantan.mongodb"
  )
  .dependsOn(bson)
  .settings(libraryDependencies ++= Seq(
    "org.mongodb"   %  "mongodb-driver" % Versions.mongodb,
    "org.scalatest" %% "scalatest"      % Versions.scalatest % "test"
  ))
  .settings(
    initialCommands in console :=
    """
      |import kantan.mongodb._
      |import kantan.bson._
    """.stripMargin
  )


lazy val bson = project
  .settings(
    moduleName := "kantan.bson",
    name       := "bson"
  )
  .enablePlugins(PublishedPlugin, spray.boilerplate.BoilerplatePlugin)
  .settings(libraryDependencies ++= Seq(
    "com.nrinaudo"  %% "kantan.codecs"      % Versions.kantanCodecs,
    "org.mongodb"   %  "bson"               % Versions.mongodb,
    "org.scalatest" %% "scalatest"          % Versions.scalatest % "test"
  ))
  .laws("laws")

lazy val laws = project
  .settings(
    moduleName := "kantan.bson-laws",
    name       := "laws"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(bson)
  .settings(libraryDependencies += "com.nrinaudo" %% "kantan.codecs-laws" % Versions.kantanCodecs)

lazy val generic = project
  .settings(
    moduleName := "kantan.bson-generic",
    name       := "generic"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(bson, laws % "test")
  .settings(libraryDependencies ++= Seq(
    "com.nrinaudo"  %% "kantan.codecs-shapeless"      % Versions.kantanCodecs,
    "org.scalatest" %% "scalatest"                    % Versions.scalatest    % "test",
    "com.nrinaudo"  %% "kantan.codecs-shapeless-laws" % Versions.kantanCodecs % "test"
  ))

lazy val jodaTime = Project(id = "joda-time", base = file("joda-time"))
  .settings(
    moduleName := "kantan.bson-joda-time",
    name       := "joda-time"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(bson, laws % "test")
  .settings(libraryDependencies ++= Seq(
    "com.nrinaudo"  %% "kantan.codecs-joda-time"      % Versions.kantanCodecs,
    "com.nrinaudo"  %% "kantan.codecs-joda-time-laws" % Versions.kantanCodecs % "test",
    "org.scalatest" %% "scalatest"                    % Versions.scalatest    % "test"
  ))

lazy val java8 = project
  .settings(
    moduleName    := "kantan.bson-java8",
    name          := "java8"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(bson, laws % "test")
  .settings(libraryDependencies ++= Seq(
    "com.nrinaudo"  %% "kantan.codecs-java8"      % Versions.kantanCodecs,
    "com.nrinaudo"  %% "kantan.codecs-java8-laws" % Versions.kantanCodecs % "test",
    "org.scalatest" %% "scalatest"                % Versions.scalatest    % "test"
  ))
