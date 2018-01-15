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
package ops

final class BsonValueDecoderOps(val value: BsonValue) extends AnyVal {
  def decodeBson[A: BsonValueDecoder]: DecodeResult[A] = BsonValueDecoder[A].decode(value)
  def unsafeDecodeBson[A: BsonValueDecoder]: A         = decodeBson[A].get
}

trait ToBsonValueDecoderOps {
  implicit def toBsonValueDecoderOps(value: BsonValue): BsonValueDecoderOps = new BsonValueDecoderOps(value)
}

object bsonValueDecoder extends ToBsonValueDecoderOps
