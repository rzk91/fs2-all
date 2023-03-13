package com.rzk.fs2

import implicits.JsonUtil.JsonOps
import implicits.StringUtil.MoreStringOps
import implicits.TimeUtil.TimeOps

import io.circe.Json

package object implicits {

  @inline implicit def enrichTime(time: Long): TimeOps = new TimeOps(time)
  @inline implicit def enrichString(str: String): MoreStringOps = new MoreStringOps(str)
  @inline implicit def enrichJson(json: Json): JsonOps = new JsonOps(json)
}
