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
package query

sealed abstract class BitOp extends Product with Serializable

object BitOp {
  final case class And(mask: Int) extends BitOp
  final case class Or(mask: Int)  extends BitOp
  final case class Xor(mask: Int) extends BitOp

  implicit val bitEncoder: BsonDocumentEncoder[BitOp] = {
    def encode(operator: String, mask: Int): BsonDocument =
      BsonDocument(Map(operator → BsonValueEncoder[Int].encode(mask)))

    BsonDocumentEncoder.from {
      case And(mask) ⇒ encode("and", mask)
      case Or(mask)  ⇒ encode("or", mask)
      case Xor(mask) ⇒ encode("xor", mask)
    }
  }
}
