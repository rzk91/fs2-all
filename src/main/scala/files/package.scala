package com.rzk.fs2

import fs2._
import fs2.io.file.{Files, Path}

package object files {

  def reader[F[_]]: Pipe[F, Byte, String] = _.through(text.utf8.decode).through(text.lines)

  def readerWith[F[_], A](f: String => A): Pipe[F, Byte, A] = _.through(reader).map(f)

  def readFilesFromPath[F[_]](path: Path, files: List[String])(implicit F: Files[F]): Stream[F, Path] =
    Files[F].walk(path).through(filterFiles(files))

  def filterFiles[F[_]](files: List[String]): Pipe[F, Path, Path] =
    _.filter(path => files.contains(path.fileName.toString))

  def listFiles[F[_]](extensions: Set[String])(p: Path => Boolean): Pipe[F, Path, Path] =
    _.filter(path => extensions(path.extName)).filter(p)

  def fileToReader[F[_]](implicit F: Files[F]): Pipe[F, Path, String] = _.flatMap(Files[F].readAll(_)).through(reader)

  def fileToReaderWith[F[_], A](f: String => A)(implicit F: Files[F]): Pipe[F, Path, A] =
    _.flatMap(Files[F].readAll(_)).through(readerWith(f))
}
