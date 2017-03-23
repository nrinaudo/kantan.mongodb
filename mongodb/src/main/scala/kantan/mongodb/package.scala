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

package object mongodb {
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
