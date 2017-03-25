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
  type MongoResult[A] = Result[MongoError, A]
  type DecodeResult[A] = Result[DecodeError, A]

  type BsonValueDecoder[A] = Decoder[BsonValue, A, DecodeError, codecs.type]
  type BsonValueEncoder[A] = Encoder[BsonValue, A, codecs.type]
  type BsonValueCodec[A] = Codec[BsonValue, A, DecodeError, codecs.type]

  type BsonDocumentDecoder[A] = Decoder[BsonDocument, A, DecodeError, codecs.type]
  type BsonDocumentEncoder[A] = Encoder[BsonDocument, A, codecs.type]
  type BsonDocumentCodec[A] = Codec[BsonDocument, A, DecodeError, codecs.type]


  // - Mongo aliases ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // TODO: write dedicated types for some / most of these?
  type BulkWriteOptions         = com.mongodb.client.model.BulkWriteOptions
  type UpdateOptions            = com.mongodb.client.model.UpdateOptions
  type FindOneAndUpdateOptions  = com.mongodb.client.model.FindOneAndUpdateOptions
  type FindOneAndReplaceOptions = com.mongodb.client.model.FindOneAndReplaceOptions
  type RenameCollectionOptions  = com.mongodb.client.model.RenameCollectionOptions
  type InsertOneOptions         = com.mongodb.client.model.InsertOneOptions
  type InsertManyOptions        = com.mongodb.client.model.InsertManyOptions
  type DeleteOptions            = com.mongodb.client.model.DeleteOptions
  type CountOptions             = com.mongodb.client.model.CountOptions
  type IndexOptions             = com.mongodb.client.model.IndexOptions
  type MongoNamespace           = com.mongodb.MongoNamespace
  type DeleteResult             = com.mongodb.client.result.DeleteResult
  type CursorType               = com.mongodb.CursorType
  type Collation                = com.mongodb.client.model.Collation
  type MongoClientOptions       = com.mongodb.MongoClientOptions
  type MongoClientURI           = com.mongodb.MongoClientURI
  type MongoCredential          = com.mongodb.MongoCredential
  type ServerAddress            = com.mongodb.ServerAddress
  type ReadConcern              = com.mongodb.ReadConcern
  type ReadPreference           = com.mongodb.ReadPreference
  type WriteConcern             = com.mongodb.WriteConcern
  type CreateCollectionOptions  = com.mongodb.client.model.CreateCollectionOptions
  type CreateViewOptions        = com.mongodb.client.model.CreateViewOptions
}
