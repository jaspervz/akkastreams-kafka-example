lazy val commonSettings = Seq(
  name := "akkastreams kafka example",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.7"
)

resolvers ++= Seq(
  "confluent" at "http://packages.confluent.io/maven/")

lazy val AkkaStreamKafkaVersion = "0.22"

lazy val Avro4sVersion = "1.9.0"

lazy val KafkaAvroSerializerVersion = "4.1.2"

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
