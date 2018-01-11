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

package kantan.mongodb.enumeratum

import arbitrary._
import kantan.codecs.enumeratum.laws.discipline.Enumerated
import kantan.codecs.laws.discipline.SerializableTests
import kantan.mongodb.{BsonValueDecoder, BsonValueEncoder}
import kantan.mongodb.laws.discipline.BsonValueCodecTests
import org.scalatest.FunSuite
import org.typelevel.discipline.scalatest.Discipline

class EnumCodecTests extends FunSuite with Discipline {

  checkAll("BsonValueDecoder[Enumerated]", SerializableTests[BsonValueDecoder[Enumerated]].serializable)
  checkAll("BsonValueEncoder[Enumerated]", SerializableTests[BsonValueEncoder[Enumerated]].serializable)
  checkAll("BsonValueCodec[Enumerated]", BsonValueCodecTests[Enumerated].codec[String, Float])

}