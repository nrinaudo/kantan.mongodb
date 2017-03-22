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

import com.mongodb.client.{MongoCollection ⇒ MCollection}
import kantan.bson._

class MongoCollection private[mongodb] (val underlying: MCollection[BsonDocument]) {
  // - Count -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def count(): Long = underlying.count()
  def count[I: BsonDocumentEncoder](filter: I): Long = underlying.count(BsonDocumentEncoder[I].encode(filter))



  // - Find ------------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def find[I: BsonDocumentEncoder, O: BsonDocumentDecoder](filter: I): Query[O] = Query(this, filter)
  def find[O: BsonDocumentDecoder](): Query[O] = Query(this)



  // - Delete ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def deleteMany[A: BsonDocumentEncoder](filter: A): DeleteResult =
    underlying.deleteMany(BsonDocumentEncoder[A].encode(filter))

  def deleteOne[A: BsonDocumentEncoder](filter: A): DeleteResult =
    underlying.deleteOne(BsonDocumentEncoder[A].encode(filter))



  // aggregate
  // bulk write
  // createIndex

  // - Misc. -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  override def toString = s"MongoCollection(${underlying.getNamespace})"
}
