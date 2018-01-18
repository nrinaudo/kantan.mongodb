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

import kantan.codecs.shapeless.ShapelessInstances
import shapeless._
import shapeless.labelled._

trait GenericInstances extends ShapelessInstances {
  // - Product-type encoding -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val bsonHnilEncoder: BsonDocumentEncoder[HNil] = BsonDocumentEncoder.from { _ ⇒
    BsonDocument(Map.empty)
  }

  implicit def hlistBsonDocumentEncoder[K <: Symbol, H: BsonValueEncoder, T <: HList: BsonDocumentEncoder](
    implicit witness: Witness.Aux[K]
  ): BsonDocumentEncoder[FieldType[K, H] :: T] = {
    val name = witness.value.name

    BsonDocumentEncoder.from { hlist ⇒
      val head = BsonValueEncoder[H].encode(hlist.head)
      val tail = BsonDocumentEncoder[T].encode(hlist.tail)
      BsonDocument(tail.value + (name → head))
    }
  }

  implicit def hlistBsonValueEncoder[H: BsonValueEncoder]: BsonValueEncoder[H :: HNil] =
    BsonValueEncoder[H].contramap { case (h :: _) ⇒ h }

  // - Product-type decoding -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val bsonHnilDecoder: BsonDocumentDecoder[HNil] =
    BsonDocumentDecoder.fromUnsafe { _ ⇒
      HNil
    }

  implicit def bsonHlistDecoder[K <: Symbol, H: BsonValueDecoder, T <: HList: BsonDocumentDecoder](
    implicit witness: Witness.Aux[K]
  ): BsonDocumentDecoder[FieldType[K, H] :: T] = {
    val name = witness.value.name

    BsonDocumentDecoder.from { doc ⇒
      for {
        head ← BsonValueDecoder[H].decode(doc.value.getOrElse(name, BsonNull)).right
        tail ← BsonDocumentDecoder[T].decode(doc).right
      } yield field[K](head) :: tail
    }
  }

  implicit def hlistBsonValueDecoder[H: BsonValueDecoder]: BsonValueDecoder[H :: HNil] =
    BsonValueDecoder[H].map(h ⇒ h :: HNil)

}
