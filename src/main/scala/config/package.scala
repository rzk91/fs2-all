package com.rzk.fs2

import pureconfig._

package object config {

  private[config] val source: ConfigObjectSource = ConfigSource
    .resources("local.conf")
    .optional
    .withFallback(ConfigSource.default)
}
