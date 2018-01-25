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

package kantan.mongodb
package libra

import _root_.libra.Quantity
import arbitrary._
import laws.discipline._
import shapeless.HNil

class LibraCodecTests extends DisciplineSuite {

  checkAll(
    "BsonValueDecoder[Quantity[Double, HNil]]",
    SerializableTests[BsonValueDecoder[Quantity[Double, HNil]]].serializable
  )
  checkAll(
    "BsonValueEncoder[Quantity[Double, HNil]]",
    SerializableTests[BsonValueEncoder[Quantity[Double, HNil]]].serializable
  )
  checkAll("BsonValueCodec[Quantity[Double, HNil]]", BsonValueCodecTests[Quantity[Double, HNil]].codec[String, Float])

  checkAll(
    "BsonValueDecoder[Quantity[Int, HNil]]",
    SerializableTests[BsonValueDecoder[Quantity[Int, HNil]]].serializable
  )
  checkAll(
    "BsonValueEncoder[Quantity[Int, HNil]]",
    SerializableTests[BsonValueEncoder[Quantity[Int, HNil]]].serializable
  )
  checkAll("BsonValueCodec[Quantity[Int, HNil]]", BsonValueCodecTests[Quantity[Int, HNil]].codec[String, Float])

}
