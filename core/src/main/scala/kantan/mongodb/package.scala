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

package kantan

import kantan.codecs._

package object mongodb {
  type Success[A] = Result.Success[A]
  val Success: Result.Success.type = Result.Success

  type Failure[A] = Result.Failure[A]
  val Failure: Result.Failure.type = Result.Failure

  /** Represents the result of any mongo operation.
    *
    * @documentable
    */
  type MongoResult[A] = Result[MongoError, A]

  /** Represents the result of any decoding operation.
    *
    * @documentable
    */
  type DecodeResult[A] = Result[MongoError.Decode, A]

  type BsonValueDecoder[A] = Decoder[BsonValue, A, MongoError.Decode, codecs.type]
  type BsonValueEncoder[A] = Encoder[BsonValue, A, codecs.type]
  type BsonValueCodec[A]   = Codec[BsonValue, A, MongoError.Decode, codecs.type]

  type BsonDocumentDecoder[A] = Decoder[BsonDocument, A, MongoError.Decode, codecs.type]
  type BsonDocumentEncoder[A] = Encoder[BsonDocument, A, codecs.type]
  type BsonDocumentCodec[A]   = Codec[BsonDocument, A, MongoError.Decode, codecs.type]

  // - Mongo aliases ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // TODO: write dedicated types for these. They are kind of non-critical at this stage and rather a lot of work,
  // but need to be sorted out eventually.
  type MongoClientOptions = com.mongodb.MongoClientOptions
  type MongoClientURI     = com.mongodb.MongoClientURI
  type MongoCredential    = com.mongodb.MongoCredential
  type ReadPreference     = com.mongodb.ReadPreference
}
