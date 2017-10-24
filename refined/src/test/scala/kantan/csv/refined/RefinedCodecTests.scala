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

package kantan.mongodb.refined

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import kantan.codecs.laws.discipline.SerializableTests
import kantan.mongodb._
import kantan.mongodb.laws.discipline.BsonValueCodecTests
import kantan.mongodb.refined.arbitrary._
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.typelevel.discipline.scalatest.Discipline

class RefinedCodecTests extends FunSuite with GeneratorDrivenPropertyChecks with Discipline {
  checkAll("BsonValueCodec[Int Refined Positive]", BsonValueCodecTests[Int Refined Positive].codec[String, Float])

  checkAll(
    "BsonValueEncoder[Int Refined Positive]",
    SerializableTests[BsonValueEncoder[Int Refined Positive]].serializable
  )
  checkAll(
    "BsonValueDecoder[Int Refined Positive]",
    SerializableTests[BsonValueDecoder[Int Refined Positive]].serializable
  )
}
