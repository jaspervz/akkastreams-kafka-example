import java.util.UUID
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import com.sksamuel.avro4s.RecordFormat
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.{AbstractKafkaSchemaSerDeConfig, KafkaAvroSerializer}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

object KafkaProducer {
  lazy private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher

    val done: Future[Done] = source.runWith(sink)

    done.onComplete(_ => system.terminate())
  }

  private def source: Source[ProducerRecord[String, AnyRef], NotUsed] = {
    val topic = config.getString("topic")

    Source(1 to 5)
      .map(_.toString)
      .map { i =>
        val example = SampleEvent(UUID.randomUUID().toString, s"message $i")
        val format = RecordFormat[SampleEvent]
        val value = format.to(example)
        new ProducerRecord[String, AnyRef](topic, value)
      }
  }

  private def sink: Sink[ProducerRecord[String, AnyRef], Future[Done]] = {
    val producerConfig = config.getConfig("akka.kafka.producer")
    val bootstrapServers = config.getString("bootstrapServers")

    val producerSettings = ProducerSettings(producerConfig, new StringSerializer, createAvroSerializer)
      .withBootstrapServers(bootstrapServers)

    Producer.plainSink(producerSettings)
  }

  private def createAvroSerializer: KafkaAvroSerializer = {
    // The configuration of the schema registry url must be provided for the serializer because when
    // configured in the producer settings, this setting isn't set for the serializer.
    val schemaRegistryUrl = config.getString("schemaRegistry.url")
    val serializerConfig = Map(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl)

    val kafkaAvroSerializer = new KafkaAvroSerializer
    kafkaAvroSerializer.configure(serializerConfig.asJava, false)
    kafkaAvroSerializer
  }
}
