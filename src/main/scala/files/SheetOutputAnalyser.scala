package com.rzk.fs2
package files

import config.Configuration.filesConfig
import implicits._

import cats.effect._
import fs2._

import scala.util.Try

object SheetOutputAnalyser extends IOApp.Simple {

  val stream: Stream[IO, Unit] = readFilesFromPath[IO](filesConfig.directoryPath, filesConfig.relevantFiles)
    .through(fileToReaderWith(_.split(',').toList))
    .filter(line => line.headOption.exists(_.startsWith("[BotDeviceKey")) && line.lift(1).exists(_.contains("[Stage:")))
    .collect { case _ :: line :: Nil =>
      val TimestampRegex = """.*At (\d+):.*""".r
      val SheetDiffRegex = """.*Gross diff = ([-\d]+); Net diff = ([-\d]+);.*""".r

      (
        line,
        TimestampRegex.findFirstIn(line).flatMap(_.asLong).getOrElse(0L), // timestamp
        line match {
          case SheetDiffRegex(grossDiff, netDiff) => Try((grossDiff.toLong, netDiff.toLong)).toOption
          case _                                  => None
        }
      )
    }
    .evalTap {
      case (p, t, Some((g, n))) => IO.println(s"[$p][$t] gross diff = $g; net diff = $n")
      case _                    => IO.unit
    }
    .filter { case (_, _, opt) => opt.exists { case (g, _) => g > 300 || g < 0 } }
    .evalMap(x => IO.println(s"ABNORMAL: $x"))

  override def run: IO[Unit] = stream.compile.count.flatMap(c => IO.println(s"Total lines = $c"))
}
