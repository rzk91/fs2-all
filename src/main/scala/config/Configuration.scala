package com.rzk.fs2
package config

import com.typesafe.scalalogging.LazyLogging
import fs2.io.file.Path
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import pureconfig.generic.auto._

object Configuration extends LazyLogging {

  logger.debug(s"Effective configuration: ${source.config().map(_.root.render)}")

  // Case classes
  case class KafkaTopics(
    findingCommands: String,
    ticketCommands: String,
    productionCommands: String,
    findingResetCommands: String
  )

  case class KafkaAuth(
    required: Boolean,
    securityProtocol: String,
    saslMechanism: String,
    jaasConfig: String
  ) {

    def asPropertyMap: Map[String, String] = if (required) {
      Map(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> securityProtocol,
        SaslConfigs.SASL_MECHANISM                   -> saslMechanism,
        SaslConfigs.SASL_JAAS_CONFIG                 -> jaasConfig
      )
    } else Map.empty
  }

  case class Kafka(
    bootstrapServers: List[String],
    auth: KafkaAuth,
    groupIdPrefix: String,
    topics: KafkaTopics,
    maxBatch: Int
  )

  case class Name(value: String) extends AnyVal
  case class User(name: Name)

  case class Files(directoryPath: Path, fileExtensions: Set[String])

  case class Config(kafka: Kafka, user: User, files: Files)

  // Config objects
  lazy val completeConfig: Config = source.loadOrThrow[Config]
  lazy val kafkaConfig: Kafka = completeConfig.kafka
  lazy val userConfig: User = completeConfig.user
  lazy val filesConfig: Files = completeConfig.files

  // Helper methods
  def kafkaGroupId(consumer: Boolean): String =
    s"${kafkaConfig.groupIdPrefix}-${userConfig.name.value}-kafka-" +
    s"${if (consumer) "reader" else "writer"}"
}
