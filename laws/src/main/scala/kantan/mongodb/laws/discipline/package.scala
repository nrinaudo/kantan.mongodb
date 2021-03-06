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
package laws

import kantan.codecs.laws.discipline._

package object discipline extends DisciplinePackage {
  type BsonValueDecoderTests[A] = DecoderTests[BsonValue, A, MongoError.Decode, codecs.type]
  type BsonValueEncoderTests[A] = EncoderTests[BsonValue, A, codecs.type]
  type BsonValueCodecTests[A]   = CodecTests[BsonValue, A, MongoError.Decode, codecs.type]

  type BsonDocumentDecoderTests[A] = DecoderTests[BsonDocument, A, MongoError.Decode, codecs.type]
  type BsonDocumentEncoderTests[A] = EncoderTests[BsonDocument, A, codecs.type]
  type BsonDocumentCodecTests[A]   = CodecTests[BsonDocument, A, MongoError.Decode, codecs.type]
}
