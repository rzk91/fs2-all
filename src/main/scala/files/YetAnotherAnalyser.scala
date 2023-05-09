package com.rzk.fs2
package files

import config.Configuration.filesConfig
import implicits._

import cats.effect._
import fs2._

object YetAnotherAnalyser extends IOApp.Simple {

  private val relevantFile: String = "dw006_948_opcycles.log.txt"

  val stream: Stream[IO, Unit] = readFilesFromPath[IO](filesConfig.directoryPath, List(relevantFile))
    .through(fileToReader)
    .map { s =>
      (s, s.split("Vector\\(").last.split(", ").length)
    }
    .evalTap { case (str, length) => IO.println(s"Length: $length; $str") }
    .filter { case (_, length) => length > 200 }
    .evalMap {
      case (str, length) => IO.println(s"CHECK! Length: $length; $str")
    }

  override def run: IO[Unit] = stream.compile.drain
}
