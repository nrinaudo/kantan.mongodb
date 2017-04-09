/*
 * Copyright 2017 Nicolas Rinaudo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kantan.mongodb.query

import kantan.mongodb.ops._
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

/** Makes sure that all helper function create values that can be BSON encoded. */
class HelperTests extends FunSuite with GeneratorDrivenPropertyChecks {
  test("All sort helpers should have an implicit BsonDocumentEncoder") {
    $asc("foo").encodeBson
    $desc("foo").encodeBson
    $metaTextScore("foo").encodeBson
  }

  test("All query helpers should have an implicit BsonDocumentEncoder") {
    $eq("foo", 1).encodeBson
    $ne("foo", 1).encodeBson
    $gt("foo", 1).encodeBson
    $gte("foo", 1).encodeBson
    $lt("foo", 1).encodeBson
    $lte("foo", 1).encodeBson
    $in("foo", 1, 2, 3).encodeBson
    $nin("foo", 1, 2, 3).encodeBson
    $elemMatch("foo", 1).encodeBson
    $exists("foo", true).encodeBson
    $regex("foo", "a-z".r.pattern).encodeBson
    $size("foo", 1).encodeBson
    $where("foo", "bar").encodeBson
    $mod("foo", 1L, 2L).encodeBson
    $all("foo", 1, 2, 3).encodeBson
    $bitsAllClear("foo", 1L).encodeBson
    $bitsAllSet("foo", 1L).encodeBson
    $bitsAnyClear("foo", 1L).encodeBson
    $bitsAnySet("foo", 1L).encodeBson
    $geoIntersects("foo", 1).encodeBson
    $geoWithin("foo", 1).encodeBson
  }
}
