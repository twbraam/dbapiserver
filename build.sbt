name := "db-api-server"

version := "0.1"

scalaVersion := "2.13.2"

val zioVersion = "1.0.0-RC18-2"
val zioCatsVersion = "2.0.0.0-RC13"
val http4sVersion = "1.0.0-M0+280-0e1d6f3a-SNAPSHOT"
val circeVersion = "0.13.0"
val logbackVersion = "1.2.3"
val doobieVersion = "0.9.0"
val pureConfigVersion = "0.12.3"
val log4jVersion = "1.7.26"


resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "dev.zio"         %% "zio"                       % zioVersion,
  "dev.zio"         %% "zio-interop-cats"          % zioCatsVersion,
  "dev.zio"         %% "zio-test"                  % zioVersion      % "test",
  "dev.zio"         %% "zio-test-sbt"              % zioVersion      % "test",

  "org.http4s"      %% "http4s-dsl"                % http4sVersion,
  "org.http4s"      %% "http4s-blaze-server"       % http4sVersion,
  "org.http4s"      %% "http4s-blaze-client"       % http4sVersion,
  "org.http4s"      %% "http4s-circe"              % http4sVersion,

  "io.circe"        %% "circe-generic"             % circeVersion,
  "io.circe"        %% "circe-literal"             % circeVersion,

  "org.tpolecat"    %% "doobie-core"               % doobieVersion,
  "org.tpolecat"    %% "doobie-postgres"           % doobieVersion,
  "org.tpolecat"    %% "doobie-specs2"             % doobieVersion,
  "org.tpolecat"    %% "doobie-hikari"             % doobieVersion,

  "ch.qos.logback"        %  "logback-classic"     % logbackVersion,
  "org.slf4j"             % "slf4j-log4j12"        % log4jVersion,
  "com.github.pureconfig" %% "pureconfig"          % pureConfigVersion
)