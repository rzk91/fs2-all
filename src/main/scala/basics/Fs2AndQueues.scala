package com.rzk.fs2
package basics

import cats.effect._
import cats.effect.std.Queue
import cats.syntax.all._
import fs2.Stream

object Fs2AndQueues extends IOApp.Simple {

  val streamFromList: Stream[IO, Int] = Stream(List(1, 2, 3, 4, 5): _*)

  private val queue = for {
    queue <- Queue.unbounded[IO, Option[Int]]
    streamFromQueue = Stream.fromQueueNoneTerminated(queue)
    _      <- Seq(Some(1), Some(2), Some(3), None).map(queue.offer).sequence
    result <- streamFromQueue.compile.toList
  } yield result

  def run: IO[Unit] = for {
    _ <- queue.flatMap(IO.println)
    _ <- streamFromList.evalMap(IO.println).compile.drain
  } yield ()

}
