package com.rzk.fs2

import fs2.io.file.Path
import pureconfig._

import java.nio.file.Paths

package object config {

  private[config] val source: ConfigObjectSource = ConfigSource
    .resources("local.conf")
    .optional
    .withFallback(ConfigSource.default)

  implicit val filePathReader: ConfigReader[Path] = ConfigReader[String].map(s => Path.fromNioPath(Paths.get(s)))
}
