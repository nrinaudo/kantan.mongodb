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

package kantan.mongodb.laws.discipline

import arbitrary._
import kantan.codecs.laws.discipline.EncoderTests
import kantan.mongodb._
import kantan.mongodb.laws.{BsonValueEncoderLaws, LegalBsonValue}
import org.scalacheck.Arbitrary

object BsonValueEncoderTests {
  def apply[A: BsonValueEncoderLaws: Arbitrary](implicit al: Arbitrary[LegalBsonValue[A]]): BsonValueEncoderTests[A] =
    EncoderTests[BsonValue, A, codecs.type]
}
