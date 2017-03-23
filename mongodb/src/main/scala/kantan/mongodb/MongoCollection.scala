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
import scala.collection.JavaConverters._
// TODO:
// - distinct
// - findOneAndReplace
// - findOneAndUpdate
// - mapReduce
// - replaceOne
// - updateMany
// - updateOne

class MongoCollection[A] private[mongodb] (val underlying: MCollection[BsonDocument]) {
  // - Count -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def count(): Long = underlying.count()
  def count[I: BsonDocumentEncoder](filter: I): Long = underlying.count(BsonDocumentEncoder[I].encode(filter))
  def countWith[I: BsonDocumentEncoder](filter: I)(options: CountOptions): Long =
    underlying.count(BsonDocumentEncoder[I].encode(filter), options)



  // - Aggregate -------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def aggregate[I: BsonDocumentEncoder, O: BsonDocumentDecoder](filters: I*): AggregateQuery[O] =
    AggregateQuery.from(underlying.aggregate(filters.map(BsonDocumentEncoder[I].encode).asJava))


  // - Indexes ---------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def createIndex[I: BsonDocumentEncoder](keys: I): String = underlying.createIndex(BsonDocumentEncoder[I].encode(keys))
  def createIndexWith[I: BsonDocumentEncoder](keys: I)(options: IndexOptions): String =
    underlying.createIndex(BsonDocumentEncoder[I].encode(keys), options)
  def indexes[O: BsonDocumentDecoder](): IndexQuery[O] = IndexQuery.from(underlying.listIndexes(classOf[BsonDocument]))
  def dropIndex[I: BsonDocumentEncoder](keys: I): Unit = underlying.dropIndex(BsonDocumentEncoder[I].encode(keys))
  def dropIndex(name: String): Unit = underlying.dropIndex(name)
  def dropIndexes(): Unit = underlying.dropIndexes()



  // - Find ------------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def find[F: BsonDocumentEncoder](filter: F)(implicit da: BsonDocumentDecoder[A]): FindQuery[A] =
    FindQuery.from(underlying.find(BsonDocumentEncoder[F].encode(filter)))
  def find()(implicit da: BsonDocumentDecoder[A]): FindQuery[A] =
    FindQuery.from(underlying.find())

  def findOneAndDelete[F: BsonDocumentEncoder](filter: F)(implicit da: BsonDocumentDecoder[A]): DecodeResult[A] =
    da.decode(underlying.findOneAndDelete(BsonDocumentEncoder[F].encode(filter)))




  // - Delete ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def deleteMany[F: BsonDocumentEncoder](filter: F): DeleteResult =
    underlying.deleteMany(BsonDocumentEncoder[F].encode(filter))

  def deleteManyWith[F: BsonDocumentEncoder](filter: F)(options: DeleteOptions): DeleteResult =
      underlying.deleteMany(BsonDocumentEncoder[F].encode(filter), options)

  def deleteOne[F: BsonDocumentEncoder](filter: F): DeleteResult =
    underlying.deleteOne(BsonDocumentEncoder[F].encode(filter))

  def deleteOneWith[F: BsonDocumentEncoder](filter: F)(options: DeleteOptions): DeleteResult =
    underlying.deleteOne(BsonDocumentEncoder[F].encode(filter), options)



  // - Insert ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def insertMany(documents: A*)(implicit ea: BsonDocumentEncoder[A]): Unit =
    underlying.insertMany(documents.map(ea.encode).toList.asJava)

  def insertManyWith(documents: A*)(options: InsertManyOptions)(implicit ea: BsonDocumentEncoder[A]): Unit =
    underlying.insertMany(documents.map(ea.encode).toList.asJava, options)

  def insertOne(document: A)(implicit ea: BsonDocumentEncoder[A]): Unit =
    underlying.insertOne(ea.encode(document))

  def insertOneWith(document: A)(options: InsertOneOptions)(implicit ea: BsonDocumentEncoder[A]): Unit =
    underlying.insertOne(ea.encode(document), options)



  // - Misc. -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  override def toString = s"MongoCollection($namespace)"

  def drop(): Unit = underlying.drop()

  def namespace: MongoNamespace = underlying.getNamespace
  def rename(db: String, name: String): Unit = underlying.renameCollection(new MongoNamespace(db, name))
  def renameWith(db: String, name: String)(options: RenameCollectionOptions): Unit =
    underlying.renameCollection(new MongoNamespace(db, name), options)

  def readConcern: ReadConcern = underlying.getReadConcern
  def writeConcern: WriteConcern = underlying.getWriteConcern
  def readPreference: ReadPreference = underlying.getReadPreference

  def withReadConcern(concern: ReadConcern): MongoCollection[A] =
    new MongoCollection(underlying.withReadConcern(concern))

  def withReadPreference(Preference: ReadPreference): MongoCollection[A] =
    new MongoCollection(underlying.withReadPreference(Preference))

  def withWriteConcern(concern: WriteConcern): MongoCollection[A] =
    new MongoCollection(underlying.withWriteConcern(concern))
}
