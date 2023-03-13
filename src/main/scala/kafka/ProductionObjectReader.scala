package com.rzk.fs2
package kafka

import config.Configuration.kafkaConfig
import implicits._

import cats.effect._
import fs2.Stream
import io.circe.optics.JsonPath.root

object ProductionObjectReader extends IOApp.Simple {

  def stream: Stream[IO, Unit] = readJsonFromTopics(kafkaConfig.topics.productionCommands)
    .filter(_.stringEquals("deviceId", "1068", Some(root.botDeviceKey)))
    .dropWhile(_.numberLessThan("startOfDay", 1677650400000L))
    .takeThrough(_.numberLessThan("startOfDay", 1678600800000L))
    .evalTap(
      logJsonWithTimestamp(_)(_.longOption("startOfDay"), _.zoneIdOption("zoneId"))
    )
    .foldMap { json =>
      (
        json.int("grossInline", 0, Some(root.sheets)),
        json.int("grossPerfecting", 0, Some(root.sheets)),
        json.int("netInline", 0, Some(root.sheets)),
        json.int("netPerfecting", 0, Some(root.sheets))
      )
    }
    .map { case (totalGrossInline, totalGrossPerfecting, totalNetInline, totalNetPerfecting) =>
      IO.println(
        List(
          s"Gross inline = $totalGrossInline",
          s"Gross perfecting = $totalGrossPerfecting",
          s"Gross total = ${totalGrossInline + totalGrossPerfecting}",
          s"Net inline = $totalNetInline",
          s"Net perfecting = $totalNetPerfecting",
          s"Net total = ${totalNetInline + totalNetPerfecting}"
        ).mkString("\n")
      )
    }

  def run: IO[Unit] = stream.compile.drain
}
