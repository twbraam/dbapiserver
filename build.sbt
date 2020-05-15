name := "db-api-server"

version := "0.1"

scalaVersion := "2.13.2"

val http4sVersion = "1.0.0-M0+280-0e1d6f3a-SNAPSHOT"
val circeVersion = "0.13.0"
val logbackVersion = "1.2.3"
val doobieVersion = "0.9.0"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s"      %% "http4s-dsl"                % http4sVersion,
  "org.http4s"      %% "http4s-blaze-server"       % http4sVersion,
  "org.http4s"      %% "http4s-blaze-client"       % http4sVersion,
  "org.http4s"      %% "http4s-circe"              % http4sVersion,
  "io.circe"        %% "circe-generic"             % circeVersion,
  "io.circe"        %% "circe-literal"             % circeVersion,
  "ch.qos.logback"  %  "logback-classic"           % logbackVersion,
  "org.tpolecat"    %% "doobie-core"               % doobieVersion,
  "org.tpolecat"    %% "doobie-postgres"           % doobieVersion,
  "org.tpolecat"    %% "doobie-specs2"             % doobieVersion


)