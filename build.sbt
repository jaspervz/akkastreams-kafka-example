lazy val commonSettings = Seq(
  name := "akkastreams kafka example",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.13.3",
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xfatal-warnings",
    "-Ywarn-value-discard",
    "-Xlint:missing-interpolator"
  )
)

resolvers ++= Seq(
  "confluent" at "http://packages.confluent.io/maven/")

lazy val AkkaStreamKafkaVersion = "2.0.2"

lazy val Avro4sVersion = "3.1.1"

lazy val KafkaAvroSerializerVersion = "5.5.0"

lazy val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback"        % "logback-classic"        % LogbackVersion,
      "com.typesafe.akka"     %% "akka-stream-kafka"     % AkkaStreamKafkaVersion,
      "io.confluent"          %  "kafka-avro-serializer" % KafkaAvroSerializerVersion,
      "com.sksamuel.avro4s"   %% "avro4s-core"           % Avro4sVersion
    )
  )
