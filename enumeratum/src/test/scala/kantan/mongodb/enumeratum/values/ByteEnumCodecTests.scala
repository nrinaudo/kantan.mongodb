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

package kantan.mongodb.enumeratum.values

import kantan.codecs.enumeratum.laws.discipline._
import kantan.codecs.laws.discipline.SerializableTests
import kantan.mongodb.{BsonValueDecoder, BsonValueEncoder}
import kantan.mongodb.enumeratum.arbitrary._
import kantan.mongodb.laws.discipline.BsonValueCodecTests
import org.scalatest.FunSuite
import org.typelevel.discipline.scalatest.Discipline

class ByteEnumCodecTests extends FunSuite with Discipline

// These aren't supported, because we don't have encoders and decoders for Byte
// See https://github.com/nrinaudo/kantan.mongodb/issues/26
/*
  checkAll("BsonValueDecoder[EnumeratedByte]", SerializableTests[BsonValueDecoder[EnumeratedByte]].serializable)
  checkAll("BsonValueEncoder[EnumeratedByte]", SerializableTests[BsonValueEncoder[EnumeratedByte]].serializable)
  checkAll("BsonValueCodec[EnumeratedByte]", BsonValueCodecTests[EnumeratedByte].codec[String, Float])
 */