# akka-streams-kafka-example
An example project using [Akka Streams](https://doc.akka.io/docs/akka/current/stream/index.html?language=scala)
with [Kafka](https://kafka.apache.org/) and
a [Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html).
 
It uses the [Alpakka Kafka Connector](https://doc.akka.io/docs/akka-stream-kafka/current/home.html) to write messages
in [Avro](https://avro.apache.org/) format.

The [Alpakka Kafka Connector](https://doc.akka.io/docs/akka-stream-kafka/current/home.html) documentation
provides clear examples on how to use [Akka Streams](https://doc.akka.io/docs/akka/current/stream/index.html?language=scala)
with [Kafka](https://kafka.apache.org/). It doesn't provide an example on how to use Akka Streams with [Avro](https://avro.apache.org/) and a [Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html).

This project provides an example on how to do this.

Kafka itself is agnostic in regard to the message format, but Avro with a Schema Registry is the preferred solution.
A message format with a schema like e.g. [Avro](https://avro.apache.org/) and a [Schema Registry](https://docs.confluent.io/current/schema-registry/docs/index.html) or [Protocol buffers](https://developers.google.com/protocol-buffers/)
compared to a message format without a schema
like JSON has the advantage that it is known what the message contains. Also backwards compatiblity can be maintained when
evolving the schema.

Using Avro with a Schema Registry has the advantage that the schema definition doesn't has to be added to every single message,
but the schema is registered in the Schema Registry and a reference to the schema is stored in the messages.

[avro4s](https://github.com/sksamuel/avro4s) is used in this example project to automatically
generate the schema from the Scala case class and to serialize and deserialize the case class.


## Running the project
To run the project, you need to have [Docker Compose](https://docs.docker.com/compose/) installed. In the root directory of the project you do a `docker-compose up` and wait
until Zookeeper, Kafka and the Schema Registry have been started. Do a `docker ps` to verify that you have a running Zookeeper, Kafka,
and Schema Registry.

After that you can start the producer with `sbt "runMain KafkaProducer"`
and the consumer with `sbt "runMain KafkaConsumer"`.

The sample producer `KafkaProducer` will create a few instances of `SampleEvent` and serialize them in Avro format and send them to the 
Kafka topic `mytopic`.

The sample consumer `KafkaConsumer` will read from the topic `mytopic` and deserialize the messages into instances of SampleEvent and log these instances.

In this example project the consumer will automatically stop after 30 seconds.
