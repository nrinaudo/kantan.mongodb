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
package generic

import kantan.codecs.shapeless.laws._
import laws.LegalBsonDocument
import laws.discipline._, arbitrary._
import org.scalacheck.Arbitrary

// Shapeless' Lazy generates code with Null that we need to ignore.
@SuppressWarnings(Array("org.wartremover.warts.Null"))
class DerivedBsonDocumentCodecTests extends DisciplineSuite {
  case class Simple(i: Int)
  case class Complex(i: Int, b: Boolean, c: Option[Double])

  implicit val arbLegal: Arbitrary[LegalBsonDocument[Or[Complex, Simple]]] =
    arbLegalValue(
      (o: Or[Complex, Simple]) ⇒
        o match {
          case Left(Complex(i, b, c)) ⇒
            BsonDocument(
              Map(
                "a" → BsonDocument(
                  Map("i" → BsonInt(i), "b" → BsonBoolean(b), "c" → c.fold[BsonValue](BsonNull)(BsonDouble.apply))
                )
              )
            )
          case Right(Simple(i)) ⇒
            BsonDocument(Map("b" → BsonDocument(Map("i" → BsonInt(i)))))
      }
    )

  checkAll(
    "DerivedBsonDocumentCodec[Or[Complex, Simple]]",
    BsonDocumentCodecTests[Or[Complex, Simple]].codec[Byte, Float]
  )
}
