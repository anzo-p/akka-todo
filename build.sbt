name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

val akkaVersion      = "2.6.19"
val akkaHttpVersion  = "10.2.8"
val cassandraVersion = "0.91"

libraryDependencies ++= Seq(
  "com.typesafe.akka"                  %% "akka-actor"                          % akkaVersion,
  "com.typesafe.akka"                  %% "akka-stream"                         % akkaVersion,
  "com.typesafe.akka"                  %% "akka-remote"                         % akkaVersion,
  "com.typesafe.akka"                  %% "akka-cluster"                        % akkaVersion,
  "com.typesafe.akka"                  %% "akka-cluster-tools"                  % akkaVersion,
  "com.typesafe.akka"                  %% "akka-persistence"                    % akkaVersion,
  "com.typesafe.akka"                  %% "akka-persistence-query"              % akkaVersion,
  "com.typesafe.akka"                  %% "akka-protobuf"                       % akkaVersion,
  "com.typesafe.akka"                  %% "akka-persistence-cassandra"          % cassandraVersion,
  "com.typesafe.akka"                  %% "akka-persistence-cassandra-launcher" % cassandraVersion % Test,
  "com.google.protobuf"                % "protobuf-java"                        % "3.21.5",
  "com.typesafe.akka"                  %% "akka-http"                           % akkaHttpVersion,
  "com.typesafe.akka"                  %% "akka-http-spray-json"                % akkaHttpVersion,
  "com.typesafe.akka"                  %% "akka-http-testkit"                   % akkaHttpVersion % "test",
  "com.softwaremill.akka-http-session" %% "core"                                % "0.7.0",
  "com.softwaremill.akka-http-session" %% "jwt"                                 % "0.7.0", // optional
  "org.scalatest"                      %% "scalatest"                           % "3.0.3" % "test"
)
