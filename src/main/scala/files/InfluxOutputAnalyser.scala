package com.rzk.fs2
package files

import config.Configuration.filesConfig
import implicits._

import cats.effect._
import fs2._

object InfluxOutputAnalyser extends IOApp.Simple {

  private val relevantFile: String = "sf29_anothertest_influx.log.txt"

  val stream: Stream[IO, Unit] = readFilesFromPath[IO](filesConfig.directoryPath, List(relevantFile))
    .through(fileToReaderWith(_.split(',').toList))
//    .drop(4) // Remove params points
    .map {
      case head :: tail => head.drop(60) :: tail // Trim lines
      case Nil => Nil
    }
    .collect {
      case head :: tail if head.contains("(times") =>
        (
          tail,
          tail(3).asLong.getOrElse(-1L), // timestamp
          tail
            .find(_.contains("standBy"))
            .flatMap(_.split("->").last.asDouble)
            .getOrElse(0.0),
          tail
            .find(_.contains("jobCount"))
            .flatMap(_.split("->").last.asDouble)
            .getOrElse(0.0)
        )
    }
    .evalTap {
      case (line, _, d, v) if d < 0 || v < 0 => IO.println(s"ABNORMAL: [$line]")
      case (line, _, _, _)                   => IO.println(s"[$line]")
    }
    .foldMap { case (_, _, d, v) => (d, v) }
    .evalMap { case (d, v) =>
      IO.println(
        s"Total time = $d (in hours: ${hours(d)}); " +
        s"Total jobs: $v; Average time = ${d / v} (in hours: ${hours(d / v)})"
      )
    }

  private def hours(millis: Double): Double = millis / 1000 / 3600

  override def run: IO[Unit] = stream.compile.drain
}
