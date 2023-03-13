package com.rzk.fs2
package implicits

import java.time.ZoneId
import scala.util.Try

object StringUtil {

  class MoreStringOps(private val str: String) extends AnyVal {
    // Safe conversion methods
    def asInt: Option[Int] = Try(str.trim.toInt).toOption
    def asLong: Option[Long] = Try(str.trim.toLong).toOption
    def asDouble: Option[Double] = Try(str.toDouble).toOption

    def asBoolean: Option[Boolean] =
      Try(str.trim.toLowerCase).collect {
        case v: String if v == "true" || v == "1"  => true
        case v: String if v == "false" || v == "0" => false
      }.toOption

    // Other helper methods
    def toOption: Option[String] = if (str.isBlank) None else Some(str)
    def hasValue: Boolean = toOption.isDefined

    def zoneIdOption: Option[ZoneId] = Try(ZoneId.of(str)).toOption
  }
}
