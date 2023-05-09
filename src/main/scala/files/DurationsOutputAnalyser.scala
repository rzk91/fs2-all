package com.rzk.fs2
package files

import config.Configuration.filesConfig
import implicits._

import cats.effect._
import fs2._

object DurationsOutputAnalyser extends IOApp.Simple {

  private val relevantFile: String = "sf29_fix_times_count.log.txt"

  val stream: Stream[IO, Unit] = readFilesFromPath[IO](filesConfig.directoryPath, List(relevantFile))
    .through(fileToReaderWith(_.split(',').toList))
    .map {
      case head :: tail => head.drop(60) :: tail // Trim lines
      case Nil          => Nil
    }
    .filter(_.headOption.exists(_.startsWith("[BotDeviceKey")))
    .collect {
      case _ :: second :: tail if second.contains("ProgramDurations") =>
        (
          second,
          tail(2).asLong.getOrElse(-1L), // timestamp
          tail
            .dropWhile(!_.startsWith("DayCycles"))
            .head
            .split('(')
            .last
            .asLong
            .getOrElse(0L)
        )
    }
    .evalTap { case (p, t, v) => IO.println(s"[$p][$t] make readies = $v") }
    .foldMap(_._3)
    .evalMap(x => IO.println(s"Total make-ready count = $x"))

  override def run: IO[Unit] = stream.compile.drain
}
