import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.sksamuel.avro4s.RecordFormat
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.{AbstractKafkaSchemaSerDeConfig, KafkaAvroDeserializer}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

object KafkaConsumer {
  private val log = LoggerFactory.getLogger(KafkaConsumer.getClass)

  lazy private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher

    val control = source.toMat(Sink.ignore)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)

    val drainingControl = control.run()

    // Clean shutdown after 30 seconds
    val done = Future {
      Thread.sleep(30000)
      drainingControl.drainAndShutdown()
    }

    done.onComplete(_ => system.terminate())
  }

  private def source(implicit executionContext: ExecutionContext, system: ActorSystem): Source[Done, Consumer.Control] = {
    val consumerConfig = config.getConfig("akka.kafka.consumer")
    val committerConfig = config.getConfig( CommitterSettings.configPath )
    val bootstrapServers = config.getString("bootstrapServers")
    val topic = config.getString("topic")
    val groupId = config.getString("consumer.groupId")

    val consumerSettings =
      ConsumerSettings(consumerConfig, new StringDeserializer, createAvroDeserializer)
        .withBootstrapServers(bootstrapServers)
        .withGroupId(groupId)

        // To start reading for the beginning also when the consumer is started after the producer
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(10) { msg =>
        processMessage(msg.record.key, msg.record.value).map(_ => msg.committableOffset)
      }
      .via(Committer.flow(CommitterSettings(committerConfig)))
  }

  private def processMessage(key: String, value: AnyRef): Future[Done] = {
    Future.successful {
      val record = value.asInstanceOf[GenericRecord]
      val format = RecordFormat[SampleEvent]
      val sampleEvent = format.from(record)

      log.info(s"Received $sampleEvent")
      Done
    }
  }

  def createAvroDeserializer: KafkaAvroDeserializer = {
    // The configuration of the schema registry url must be provided for the deserializer because when
    // configured in the consumer settings, this setting isn't set for the deserializer.
    val schemaRegistryUrl = config.getString("schemaRegistry.url")
    val deserializerConfig = Map(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl)

    val kafkaAvroDeserializer = new KafkaAvroDeserializer
    kafkaAvroDeserializer.configure(deserializerConfig.asJava, false)
    kafkaAvroDeserializer
  }
}
