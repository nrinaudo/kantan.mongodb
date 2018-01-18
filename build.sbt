kantanProject in ThisBuild := "mongodb"
startYear in ThisBuild     := Some(2017)

// - root projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val root = Project(id = "kantan-mongodb", base = file("."))
  .settings(moduleName := "root")
  .enablePlugins(UnpublishedPlugin)
  .settings(
    initialCommands in console :=
      """
      |import kantan.mongodb._
      |import kantan.mongodb.ops._
      |import kantan.mongodb.query._
      |import kantan.mongodb.refined._
    """.stripMargin
    //import kantan.mongodb.generic._
  )
  .aggregate(core, enumeratum, generic, jodaTime, laws, query, refined)
  .aggregateIf(java8Supported)(java8)
  .dependsOn(core, generic, query, refined)

lazy val docs = project
  .enablePlugins(DocumentationPlugin)
  .settings(
    unidocProjectFilter in (ScalaUnidoc, unidoc) :=
      inAnyProject -- inProjectsIf(!java8Supported)(java8)
  )
  .dependsOn(core, enumeratum, jodaTime, query, generic, refined)
  .dependsOnIf(java8Supported)(java8)

lazy val core = project
  .settings(
    moduleName := "kantan.mongodb",
    name       := "mongodb"
  )
  .enablePlugins(PublishedPlugin, BuildInfoPlugin, spray.boilerplate.BoilerplatePlugin)
  .settings(
    buildInfoKeys    := Seq[BuildInfoKey](version, BuildInfoKey.action("commit") { git.gitHeadCommit.value }),
    buildInfoPackage := "kantan.mongodb"
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.nrinaudo"  %% "kantan.codecs" % Versions.kantanCodecs,
      "org.mongodb"   % "mongodb-driver" % Versions.mongodb,
      "org.scalatest" %% "scalatest"     % Versions.scalatest % "test"
    )
  )
  .laws("laws")
  .settings(
    initialCommands in console :=
      """
      |import kantan.mongodb._
    """.stripMargin
  )

lazy val query = project
  .settings(
    moduleName := "kantan.mongodb-query",
    name       := "query"
  )
  .enablePlugins(PublishedPlugin)
  .settings(libraryDependencies += "org.scalatest" %% "scalatest" % Versions.scalatest % "test")
  .dependsOn(core, laws % "test")

lazy val laws = project
  .settings(
    moduleName := "kantan.mongodb-laws",
    name       := "laws"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core)
  .settings(libraryDependencies += "com.nrinaudo" %% "kantan.codecs-laws" % Versions.kantanCodecs)

lazy val generic = project
  .settings(
    moduleName := "kantan.mongodb-generic",
    name       := "generic"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % "test")
  .settings(
    libraryDependencies ++= Seq(
      "com.nrinaudo"  %% "kantan.codecs-shapeless"      % Versions.kantanCodecs,
      "org.scalatest" %% "scalatest"                    % Versions.scalatest % "test",
      "com.nrinaudo"  %% "kantan.codecs-shapeless-laws" % Versions.kantanCodecs % "test"
    )
  )

lazy val jodaTime = Project(id = "joda-time", base = file("joda-time"))
  .settings(
    moduleName := "kantan.mongodb-joda-time",
    name       := "joda-time"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % "test")
  .settings(
    libraryDependencies ++= Seq(
      "com.nrinaudo"  %% "kantan.codecs-joda-time"      % Versions.kantanCodecs,
      "com.nrinaudo"  %% "kantan.codecs-joda-time-laws" % Versions.kantanCodecs % "test",
      "org.scalatest" %% "scalatest"                    % Versions.scalatest % "test"
    )
  )

lazy val java8 = project
  .settings(
    moduleName := "kantan.mongodb-java8",
    name       := "java8"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % "test")
  .settings(
    libraryDependencies ++= Seq(
      "com.nrinaudo"  %% "kantan.codecs-java8"      % Versions.kantanCodecs,
      "com.nrinaudo"  %% "kantan.codecs-java8-laws" % Versions.kantanCodecs % "test",
      "org.scalatest" %% "scalatest"                % Versions.scalatest % "test"
    )
  )

lazy val refined = project
  .settings(
    moduleName := "kantan.mongodb-refined",
    name       := "refined"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % "test")
  .settings(
    libraryDependencies ++= Seq(
      "com.nrinaudo"  %% "kantan.codecs-refined"      % Versions.kantanCodecs,
      "com.nrinaudo"  %% "kantan.codecs-refined-laws" % Versions.kantanCodecs % "test",
      "org.scalatest" %% "scalatest"                  % Versions.scalatest % "test"
    )
  )

lazy val enumeratum = project
  .settings(
    moduleName := "kantan.mongodb-enumeratum",
    name       := "enumeratum"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws % "test")
  .settings(
    libraryDependencies ++= Seq(
      "com.nrinaudo"  %% "kantan.codecs-enumeratum"      % Versions.kantanCodecs,
      "com.nrinaudo"  %% "kantan.codecs-enumeratum-laws" % Versions.kantanCodecs % "test",
      "org.scalatest" %% "scalatest"                     % Versions.scalatest % "test"
    )
  )
