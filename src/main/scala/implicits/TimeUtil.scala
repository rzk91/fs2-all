package com.rzk.fs2
package implicits

import java.time.{Instant, ZoneId}

object TimeUtil {

  class TimeOps(private val time: Long) extends AnyVal {

    def humanReadable(zoneId: Option[ZoneId] = None): String =
      Instant.ofEpochMilli(time).atZone(zoneId.getOrElse(ZoneId.of("UTC"))).toString
  }
}
