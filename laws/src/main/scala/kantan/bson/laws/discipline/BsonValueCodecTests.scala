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

package kantan.bson.laws.discipline

import arbitrary._
import kantan.bson._
import kantan.bson.laws.{BsonValueCodecLaws, LegalBsonValue}
import kantan.codecs.laws.discipline.CodecTests
import org.scalacheck.{Arbitrary, Cogen}

object BsonValueCodecTests {
  def apply[A: BsonValueCodecLaws: Arbitrary: Cogen](implicit al: Arbitrary[LegalBsonValue[A]])
  : BsonValueCodecTests[A] = CodecTests[BsonValue, A, DecodeError, codecs.type]
}
