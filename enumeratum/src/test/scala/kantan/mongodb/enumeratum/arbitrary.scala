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

import kantan.codecs.enumeratum.laws.discipline._
import kantan.codecs.laws.CodecValue.IllegalValue
import kantan.mongodb._
import org.scalacheck.Arbitrary, Arbitrary.{arbitrary ⇒ arb}

object arbitrary
    extends kantan.mongodb.laws.discipline.ArbitraryInstances
    with kantan.codecs.enumeratum.laws.discipline.ArbitraryInstances {

  def arbFor[A, B: BsonValueDecoder]: Arbitrary[IllegalValue[BsonValue, A, codecs.type]] =
    Arbitrary(
      arb[IllegalValue[BsonValue, B, codecs.type]].map {
        case IllegalValue(bson) ⇒ IllegalValue(bson)
      }
    )

  implicit val arbIllegalBsonEnumeratedInt: Arbitrary[IllegalValue[BsonValue, EnumeratedInt, codecs.type]] =
    arbFor[EnumeratedInt, Int]

  implicit val arbIllegalBsonEnumeratedLong: Arbitrary[IllegalValue[BsonValue, EnumeratedLong, codecs.type]] =
    arbFor[EnumeratedLong, Long]

  implicit val arbIllegalBsonEnumeratedString: Arbitrary[IllegalValue[BsonValue, EnumeratedString, codecs.type]] =
    arbFor[EnumeratedString, String]
}
