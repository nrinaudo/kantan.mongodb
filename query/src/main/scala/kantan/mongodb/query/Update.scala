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

package kantan.mongodb.query

import kantan.mongodb.{BsonDocument, BsonDocumentEncoder, BsonValue, BsonValueEncoder}

sealed trait Update extends Product with Serializable

object Update {
  // - Combination -----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Combined[L, R](left: L, right: R) extends Update {
    def &&[U <: Update](u: U): Combined[Combined[L, R], U] = Combined(this, u)
  }

  implicit def combinedEncoder[L: BsonDocumentEncoder, R: BsonDocumentEncoder]: BsonDocumentEncoder[Combined[L, R]] = {
    def merge(left: Map[String, BsonValue], right: Map[String, BsonValue])(f: (BsonValue, BsonValue) ⇒ BsonValue) =
      left.foldLeft(right) { case (acc, (op, l)) ⇒ acc.get(op).fold(acc + (op → l))(r ⇒ acc + (op → f(l, r))) }

    BsonDocumentEncoder.from {
      case Combined(left, right) ⇒
        BsonDocument(merge(BsonDocumentEncoder[L].encode(left).value, BsonDocumentEncoder[R].encode(right).value) {
          case (BsonDocument(l), BsonDocument(r)) ⇒ BsonDocument(merge(l, r)((_, r2) ⇒ r2))
          case (_, r2)                            ⇒ r2
        })
    }
  }

  // - Field updates ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Field[A](name: String, update: UpdateOperator[A]) extends Update {
    def &&[U <: Update](u: U): Combined[Field[A], U] = Combined(this, u)
  }

  implicit def fieldEncoder[A: BsonValueEncoder]: BsonDocumentEncoder[Field[A]] = BsonDocumentEncoder.from {
    case Field(name, update) ⇒
      BsonDocument(Map(update.operator → BsonDocument(Map(name → BsonValueEncoder[A].encode(update.operand)))))
  }
}
