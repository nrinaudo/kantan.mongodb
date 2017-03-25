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

package kantan.mongodb.laws.discipline

import arbitrary._
import kantan.codecs.laws.discipline.CodecTests
import kantan.mongodb._
import kantan.mongodb.laws.{BsonDocumentCodecLaws, LegalBsonDocument}
import org.scalacheck.{Arbitrary, Cogen}

object BsonDocumentCodecTests {
  def apply[A: BsonDocumentCodecLaws: Arbitrary: Cogen](implicit al: Arbitrary[LegalBsonDocument[A]])
  : BsonDocumentCodecTests[A] = CodecTests[BsonDocument, A, DecodeError, codecs.type]
}
