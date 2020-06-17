import BuildKeys._
import Boilerplate._

// ---------------------------------------------------------------------------
// Commands

addCommandAlias("release", ";+clean ;ci-release")
addCommandAlias("ci", ";project root ;reload ;+clean ;+test:compile ;+test ;+package")

// ---------------------------------------------------------------------------
// Dependencies

/** Standard FP library for Scala:
  * [[https://typelevel.org/cats/]]
  */
val CatsVersion = "2.1.1"

/** FP library for describing side-effects:
  * [[https://typelevel.org/cats-effect/]]
  */
val CatsEffectVersion = "2.1.3"

/**
 * ZIO asynchronous and concurrent programming library
 * [[https://zio.dev/]]
 */
val ZIOVersion = "1.0.0-RC18-1"

/** Newtype (opaque type) definitions:
  * [[https://github.com/estatico/scala-newtype]]
  */
val NewtypeVersion = "0.4.3"

/** First-class support for type-classes:
  * [[https://github.com/typelevel/simulacrum]]
  */
val SimulacrumVersion = "1.0.0"

/** For macros that are supported on older Scala versions.
  * Not needed starting with Scala 2.13.
  */
val MacroParadiseVersion = "2.1.0"

/** Library for unit-testing:
  * [[https://github.com/monix/minitest/]]
  */
val MinitestVersion = "2.8.2"

/** Library for property-based testing:
  * [[https://www.scalacheck.org/]]
  */
val ScalaCheckVersion = "1.14.3"

/** Compiler plugin for working with partially applied types:
  * [[https://github.com/typelevel/kind-projector]]
  */
val KindProjectorVersion = "0.11.0"

/** Compiler plugin for fixing "for comprehensions" to do desugaring w/o `withFilter`:
  * [[https://github.com/typelevel/kind-projector]]
  */
val BetterMonadicForVersion = "0.3.1"

/** Compiler plugin for silencing compiler warnings:
  * [[https://github.com/ghik/silencer]]
  */
val SilencerVersion = "1.6.0"

/**
 * Li Haoyi Ammonite repl embed:
 * [[https://ammonite.io/]]
 */
val AmmoniteVersion = "2.0.0"

/**
  * Defines common plugins between all projects.
  */
def defaultPlugins: Project ⇒ Project = pr => {
  val withCoverage = sys.env.getOrElse("SBT_PROFILE", "") match {
    case "coverage" => pr
    case _ => pr.disablePlugins(scoverage.ScoverageSbtPlugin)
  }
  withCoverage
    .enablePlugins(AutomateHeaderPlugin)
    .enablePlugins(GitBranchPrompt)
}

lazy val sharedSettings = Seq(
  projectTitle := "My CV",
  githubOwnerID := "MercurieVV",
  githubRelativeRepositoryID := "my-cv",

  organization := "viktprs.kalinins",
  scalaVersion := "2.13.1",
  // More version specific compiler options
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 =>
      Seq(
        "-Ypartial-unification",
      )
    case _ =>
      Seq(
        // Replaces macro-paradise in Scala 2.13
        "-Ymacro-annotations",
      )
  }),

    // Turning off fatal warnings for doc generation
  scalacOptions.in(Compile, doc) ~= filterConsoleScalacOptions,
  // Silence all warnings from src_managed files
  scalacOptions += "-P:silencer:pathFilters=.*[/]src_managed[/].*",

  addCompilerPlugin("org.typelevel" % "kind-projector" % KindProjectorVersion cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % BetterMonadicForVersion),
  addCompilerPlugin("com.github.ghik" % "silencer-plugin" % SilencerVersion cross CrossVersion.full),

  // ScalaDoc settings
  autoAPIMappings := true,
  scalacOptions in ThisBuild ++= Seq(
    // Note, this is used by the doc-source-url feature to determine the
    // relative path of a given source file. If it's not a prefix of a the
    // absolute path of the source file, the absolute path of that file
    // will be put into the FILE_SOURCE variable, which is
    // definitely not what we want.
    "-sourcepath", file(".").getAbsolutePath.replaceAll("[.]$", "")
  ),

  // https://github.com/sbt/sbt/issues/2654
  incOptions := incOptions.value.withLogRecompileOnMacro(false),

  // ---------------------------------------------------------------------------
  // Options for testing

  testFrameworks += new TestFramework("minitest.runner.Framework"),
  logBuffered in Test := false,
  logBuffered in IntegrationTest := false,
  // Disables parallel execution
  parallelExecution in Test := false,
  parallelExecution in IntegrationTest := false,
  testForkedParallel in Test := false,
  testForkedParallel in IntegrationTest := false,
  concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),

  // ---------------------------------------------------------------------------
  // Options meant for publishing on Maven Central

  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }, // removes optional dependencies

  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  headerLicense := Some(HeaderLicense.Custom(
    s"""|Copyright (c) 2020 the ${projectTitle.value} contributors.
        |Licensed under the Apache License, Version 2.0 (the "License");
        |you may not use this file except in compliance with the License.
        |You may obtain a copy of the License at
        |
        |    http://www.apache.org/licenses/LICENSE-2.0
        |
        |Unless required by applicable law or agreed to in writing, software
        |distributed under the License is distributed on an "AS IS" BASIS,
        |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        |See the License for the specific language governing permissions and
        |limitations under the License."""
      .stripMargin)),

  scmInfo := Some(
    ScmInfo(
      url(s"https://github.com/${githubFullRepositoryID.value}"),
      s"scm:git@github.com:${githubFullRepositoryID.value}.git"
    )),

  developers := List(
    Developer(
      id="mercurievv",
      name="Viktors Kalinins",
      email="mercurievv@gmail.com",
      url=url("https://github.com/MercurieVV")
    )),

)

lazy val root = project.in(file("."))
  .configure(defaultPlugins)
  .settings(sharedSettings)
  .settings(doNotPublishArtifact)
  .settings(
    // Try really hard to not execute tasks in parallel ffs
    Global / concurrentRestrictions := Tags.limitAll(1) :: Nil,
  )

lazy val core =
  project
  .in(file("core"))
  .settings(
    wartremoverErrors in (Compile, compile) ++= Warts.allBut(
      Wart.Any,
      Wart.AnyVal,
      Wart.Nothing,
      Wart.StringPlusAny,
      Wart.ToString,
      Wart.FinalCaseClass,
      Wart.DefaultArguments,
      Wart.Overloading
    ),
    wartremoverExcluded += sourceManaged.value,
/*
    longTest := {
      //      scalafmtCheck.value
      dependencyCheck.value
      dependencyUpdates.value
      stryker.value
    }
*/
  )
  .settings(
    name := "my-cv-core",
    libraryDependencies ++= Seq(
      "io.estatico"    %% "newtype"          % NewtypeVersion % Provided,
      "org.typelevel"  %% "simulacrum"       % SimulacrumVersion % Provided,
      "org.typelevel"  %% "cats-core"        % CatsVersion,
      "org.typelevel"  %% "cats-effect"      % CatsEffectVersion,
      "dev.zio"        %% "zio"              % ZIOVersion,
      "dev.zio"        %% "zio-streams"      % ZIOVersion,
      // For testing
      "io.monix"       %% "minitest"         % MinitestVersion % Test,
      "io.monix"       %% "minitest-laws"    % MinitestVersion % Test,
      "org.scalacheck" %% "scalacheck"       % ScalaCheckVersion % Test,
      "org.typelevel"  %% "cats-laws"        % CatsVersion % Test,
      "org.typelevel"  %% "cats-effect-laws" % CatsEffectVersion % Test,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.3" % Test,
      "io.chrisdavenport" %% "cats-scalacheck"                    % "0.3.0" % Test
    ),
  )
/*  .enablePlugins(AwsLambdaPlugin)
  .settings(
    // or, instead of the above, for just one function/handler
    retrieveManaged := true,
    lambdaName      := Some("function1"),
    handlerName := Some(
      "com.github.mercurievv.jobsearch.AppHandler::handleRequest"
    ),
    s3Bucket         := Some("mvv-lambda-jars"),
    region           := Some("eu-west-1"),
    awsLambdaMemory  := Some(192),
    awsLambdaTimeout := Some(30),
    roleArn          := Some("arn:aws:iam::173308913183:role/lambda-role")
  )
*/

libraryDependencies += {
  "com.lihaoyi" % "ammonite" % AmmoniteVersion % "test" cross CrossVersion.full
}

sourceGenerators in Test += Def.task {
  val file = (sourceManaged in Test).value / "amm.scala"
  IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
  Seq(file)
}.taskValue

// Reloads build.sbt changes whenever detected
Global / onChangedBuildSource := ReloadOnSourceChanges
