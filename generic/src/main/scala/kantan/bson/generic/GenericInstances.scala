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

package kantan.bson.generic

import kantan.bson._
import kantan.codecs.shapeless.ShapelessInstances
import shapeless._
import shapeless.labelled.{field, FieldType}

trait GenericInstances extends ShapelessInstances {
  // - Product-type encoding -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val bsonHnilEncoder: BsonDocumentEncoder[HNil] = BsonDocumentEncoder.from { _ ⇒ BsonDocument(Map.empty) }

  implicit def bsonHlistEncoder[K <: Symbol, H, T <: HList]
  (implicit witness: Witness.Aux[K], hEncoder: Lazy[BsonValueEncoder[H]], tEncoder: BsonDocumentEncoder[T]):
  BsonDocumentEncoder[FieldType[K, H] :: T] = {
    val name = witness.value.name

    BsonDocumentEncoder.from { hlist ⇒
      val head = hEncoder.value.encode(hlist.head)
      val tail = tEncoder.encode(hlist.tail)
      BsonDocument(tail.value + (name → head))
    }
  }



  // - Product-type decoding -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val bsonHnilDecoder: BsonDocumentDecoder[HNil] =
    BsonDocumentDecoder.fromSafe { _ ⇒ HNil }

  implicit def bsonHlistDecoder[K <: Symbol, H, T <: HList]
  (implicit witness: Witness.Aux[K], hDecoder: Lazy[BsonValueDecoder[H]], tDecoder: BsonDocumentDecoder[T]):
    BsonDocumentDecoder[FieldType[K, H] :: T] = {
    val name = witness.value.name

    BsonDocumentDecoder.from { doc ⇒
      for {
        head ← hDecoder.value.decode(doc.value.getOrElse(name, BsonNull))
        tail ← tDecoder.decode(doc)
      } yield field[K](head) :: tail
    }
  }


  // - Sum-type codec --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val bsonCnilDecoder: BsonDocumentDecoder[CNil] =
    cnilDecoder(c ⇒ DecodeError(s"Not a legal BSON document: $c"))
}
