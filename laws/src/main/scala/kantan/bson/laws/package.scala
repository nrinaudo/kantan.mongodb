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

package kantan.bson

import kantan.codecs.laws.{CodecLaws, CodecValue, DecoderLaws, EncoderLaws}

package object laws {
  type BsonValueDecoderLaws[A] = DecoderLaws[BsonValue, A, DecodeError, codecs.type]
  type BsonValueEncoderLaws[A] = EncoderLaws[BsonValue, A, codecs.type]
  type BsonValueCodecLaws[A] = CodecLaws[BsonValue, A, DecodeError, codecs.type]

  type BsonDocumentDecoderLaws[A] = DecoderLaws[BsonDocument, A, DecodeError, codecs.type]
  type BsonDocumentEncoderLaws[A] = EncoderLaws[BsonDocument, A, codecs.type]
  type BsonDocumentCodecLaws[A] = CodecLaws[BsonDocument, A, DecodeError, codecs.type]

  type BsonValueValue[A] = CodecValue[BsonValue, A, codecs.type]
  type LegalBsonValue[A] = CodecValue.LegalValue[BsonValue, A, codecs.type]
  type IllegalBsonValue[A] = CodecValue.IllegalValue[BsonValue, A, codecs.type]

  type BsonDocumentValue[A] = CodecValue[BsonDocument, A, codecs.type]
  type LegalBsonDocument[A] = CodecValue.LegalValue[BsonDocument, A, codecs.type]
  type IllegalBsonDocument[A] = CodecValue.IllegalValue[BsonDocument, A, codecs.type]
}