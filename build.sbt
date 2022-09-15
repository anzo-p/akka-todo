name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

val akkaVersion             = "2.6.19"
val akkaHttpVersion         = "10.2.8"
val cassandraVersion        = "0.91"
val catsVersion             = "2.8.0"
val scalaTestVersion        = "3.2.10"
val mockitoVersion          = "3.2.10.0"
val dnvriendInMemoryVersion = "2.5.15.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka"    %% "akka-actor"                 % akkaVersion,
  "com.typesafe.akka"    %% "akka-stream"                % akkaVersion,
  "com.typesafe.akka"    %% "akka-remote"                % akkaVersion,
  "com.typesafe.akka"    %% "akka-cluster"               % akkaVersion,
  "com.typesafe.akka"    %% "akka-cluster-tools"         % akkaVersion,
  "com.typesafe.akka"    %% "akka-persistence"           % akkaVersion,
  "com.typesafe.akka"    %% "akka-persistence-query"     % akkaVersion,
  "com.typesafe.akka"    %% "akka-protobuf"              % akkaVersion,
  "com.typesafe.akka"    %% "akka-persistence-cassandra" % cassandraVersion,
  "com.typesafe.akka"    %% "akka-http"                  % akkaHttpVersion,
  "com.typesafe.akka"    %% "akka-http-spray-json"       % akkaHttpVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime"            % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "org.typelevel"        %% "cats-core"                  % catsVersion
)

resolvers += Resolver.bintrayRepo("dnvriend", "maven")

libraryDependencies ++= Seq(
  "org.scalatest"             %% "scalatest"                 % scalaTestVersion % Test,
  "com.typesafe.akka"         %% "akka-testkit"              % akkaVersion,
  "com.typesafe.akka"         %% "akka-http-testkit"         % akkaHttpVersion % "test",
  "org.scalatestplus"         %% "scalacheck-1-15"           % "3.2.11.0" % Test,
  "org.scalatestplus"         %% "mockito-3-4"               % mockitoVersion % Test,
  "com.github.dnvriend"       %% "akka-persistence-inmemory" % dnvriendInMemoryVersion,
  "org.iq80.leveldb"          % "leveldb"                    % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all"             % "1.8"
)

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)
