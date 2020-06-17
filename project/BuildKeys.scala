import sbt.Def._
import sbt.{Def, settingKey}

object BuildKeys {
  /**
    * When this value is set, it means we want to test and publish a custom Scala.js
    * version, therefore we shouldn't re-publish the JVM packages.
    */
  lazy val customScalaJSVersion =
    Option(System.getenv("SCALAJS_VERSION"))

  /**
    * Human readable project title.
    *
    * Examples:
    *
    *  - Cats
    *  - Cats Effect
    *  - Monix
    */
  lazy val projectTitle =
    settingKey[String]("Human readable project title (e.g. 'Cats Effect', 'Monix', etc)")


  /**
    * Example: alexandru, monix, typelevel, etc.
    */
  lazy val githubOwnerID =
    settingKey[String]("GitHub owner ID (e.g. user_id, organization_id)")

  /**
    * Example: alexandru, monix, typelevel, etc.
    */
  lazy val githubRelativeRepositoryID =
    settingKey[String]("GitHub repository ID (e.g. project_name)")

  /**
    * Example: `alexandru/my-typelevel-library`
    */
  lazy val githubFullRepositoryID =
    Def.setting(
      s"${githubOwnerID.value}/${githubOwnerID.value}"
    )

  /**
    * Auto-detected by the build process.
    */
  lazy val needsScalaMacroParadise =
    settingKey[Boolean]("Needs Scala Macro Paradise")
}
