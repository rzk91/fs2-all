package com.rzk.fs2
package kafka

import config.Configuration._
import implicits._

import cats.effect._
import fs2.Stream
import io.circe.optics.JsonPath._

object FindingReader extends IOApp.Simple {

  def stream: Stream[IO, Unit] = readJsonFromTopics(kafkaConfig.topics.findingCommands)
    .filter(_.jsonOption("botDeviceKey").exists(_.isDefined))                    // Current deployment
    .filter(_.stringStartsWith("bot", "kba-sheetfed-", Some(root.botDeviceKey))) // Only Sheetfed bots
    .filter(_.stringEquals("$type", "create"))                                   // Only create findings
    .filter(_.booleanEquals("createCase", value = true))                         // Only cases
    .mapAsync(25)(logJson)

  def run: IO[Unit] = stream.compile.drain
}
