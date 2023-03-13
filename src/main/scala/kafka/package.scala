package com.rzk.fs2

import config.Configuration._
import implicits._

import cats.effect.IO
import fs2.Stream
import fs2.kafka._
import io.circe._
import io.circe.parser._

import java.time.ZoneId

package object kafka {

  implicit val valueDeserializer: Deserializer[IO, Json] = Deserializer.lift[IO, Json] { bytes =>
    IO.fromEither(parse(new String(bytes)))
  }

  val consumerSettings: ConsumerSettings[IO, Unit, Json] =
    ConsumerSettings[IO, Unit, Json]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(kafkaConfig.bootstrapServers.mkString(","))
      .withProperties(kafkaConfig.auth.asPropertyMap)
      .withGroupId(kafkaGroupId(consumer = true))
      .withMaxPollRecords(kafkaConfig.maxBatch)

  def logJsonWithTimestamp(record: ConsumerRecord[Unit, Json]): IO[Unit] =
    IO.println(s"[${record.timestamp.createTime.map(_.humanReadable())}] ${record.value.noSpaces}")

  def logJsonWithTimestamp(json: Json)(timestamp: Json => Option[Long], zoneId: Json => Option[ZoneId]): IO[Unit] =
    IO.println(s"[${timestamp(json).map(_.humanReadable(zoneId(json)))}] ${json.noSpaces}")

  def logJson(json: Json): IO[Unit] = IO.println(json.noSpaces)

  def readCommittableRecordsFromTopics(
    topic1: String,
    others: String*
  ): Stream[IO, CommittableConsumerRecord[IO, Unit, Json]] =
    KafkaConsumer.stream(consumerSettings).subscribeTo(topic1, others: _*).records

  def readRecordsFromTopics(topic1: String, others: String*): Stream[IO, ConsumerRecord[Unit, Json]] =
    readCommittableRecordsFromTopics(topic1, others: _*).map(_.record)

  def readJsonFromTopics(topic1: String, others: String*): Stream[IO, Json] =
    readCommittableRecordsFromTopics(topic1, others: _*).map(_.record.value)
}
