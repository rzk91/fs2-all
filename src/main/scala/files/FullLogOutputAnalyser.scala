package com.rzk.fs2
package files

import config.Configuration.filesConfig
import implicits._
import utils.TimestampedVal

import cats.effect._
import fs2._

object FullLogOutputAnalyser extends IOApp.Simple {

  private val relevantFile: String = "sf29_fulltest_1243.log.txt"

  val mainStream: Stream[IO, List[String]] = readFilesFromPath[IO](filesConfig.directoryPath, List(relevantFile))
    .through(fileToReaderWith(_.split(',').toList))
    .dropWhile(!_.exists(_.contains("[BotDeviceKey(")))
    .map {
      case head :: tail => head.drop(60) :: tail // Trim lines
      case Nil          => Nil
    }

  val influxStream: Stream[IO, TimestampedVal[Long]] = mainStream
    .collect {
      case head :: tail if head.contains("(sheetsAndSpeed") =>
        TimestampedVal(
          tail(3).asLong.getOrElse(-1L),
          tail
            .find(_.contains("netInlineDouble"))
            .flatMap(_.split("->").last.asDouble.map(_.toLong))
            .getOrElse(0L)
        )
    }

  val durationsStream: Stream[IO, TimestampedVal[Long]] = mainStream
    .collect {
      case first :: second :: tail
          if first.startsWith("[BotDeviceKey") && second.contains("ProgramDurations") && !second.contains("pending") =>
        TimestampedVal(
          tail(2).asLong.getOrElse(-1L),
          tail
            .dropWhile(!_.startsWith("DayCycles"))
            .apply(1) // .tail.head
            .asLong
            .getOrElse(0L)
        )
    }

  val joinedStream: Stream[IO, Unit] = influxStream
    .zipWith(durationsStream) {
      case (tv1, tv2) if tv1.t == tv2.t => Option(TimestampedVal(tv1.t, (tv1.v, tv2.v)))
      case _                            => None
    }
    .collect { case Some(v) => v }
    .evalTap { case TimestampedVal(t, (ns, nd)) => IO.println(s"[$t] SheetsAndSpeed = $ns; ProgramDurations = $nd") }
    .foldMap(_.v)
    .evalMap { case (totalNs, totalNd) => IO.println(s"Total 1 = $totalNs; total 2 = $totalNd ") }

  override def run: IO[Unit] = joinedStream.compile.drain
}
