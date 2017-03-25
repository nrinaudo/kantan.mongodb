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

import kantan.codecs.Result.Success

object BsonDocumentDecoder extends GeneratedBsonDocumentDecoders {
  def apply[A](implicit ev: BsonDocumentDecoder[A]): BsonDocumentDecoder[A] = macro imp.summon[BsonDocumentDecoder[A]]

  def from[A](f: BsonDocument ⇒ DecodeResult[A]): BsonDocumentDecoder[A] = new BsonDocumentDecoder[A] {
    override def decode(d: BsonDocument) = f(d)
  }

  def fromSafe[A](f: BsonDocument ⇒ A): BsonDocumentDecoder[A] =
    BsonDocumentDecoder.from(f andThen Success.apply)
}

trait BsonDocumentDecoderInstances {
  implicit val bsonDocumentDocumentDecoder: BsonDocumentDecoder[BsonDocument] = BsonDocumentDecoder.fromSafe(identity)
}
