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

class MongoCollection[A] private[mongodb] (val underlying: MCollection[BsonDocument]) {
  // - Count -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def count(): MongoResult[Long] = MongoResult(underlying.count())
  def count[I: BsonDocumentEncoder](filter: I): MongoResult[Long] =
    MongoResult(underlying.count(BsonDocumentEncoder[I].encode(filter)))
  def countWith[I: BsonDocumentEncoder](filter: I)(options: CountOptions): MongoResult[Long] =
    MongoResult(underlying.count(BsonDocumentEncoder[I].encode(filter), options))



  // - Bulk operations -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def bulkWrite(operations: BulkOperation*): MongoResult[BulkResult] =
    MongoResult(BulkResult(underlying.bulkWrite(operations.map(_.toModel).asJava)))
  def bulkWriteWith(operations: BulkOperation*)(options: BulkWriteOptions): MongoResult[BulkResult] =
    MongoResult(BulkResult(underlying.bulkWrite(operations.map(_.toModel).asJava, options)))


  // - Aggregate / Map-Reduce ------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // TODO: make safe
  def aggregate[F: BsonDocumentEncoder, O: BsonDocumentDecoder](filters: F*): AggregateQuery[O] =
    AggregateQuery.from(underlying.aggregate(filters.map(BsonDocumentEncoder[F].encode).asJava))

  // TODO: make safe
  def mapReduce[O: BsonDocumentDecoder](map: String, reduce: String): MapReduceQuery[O] =
    MapReduceQuery.from(underlying.mapReduce(map, reduce))



  // - Distinct --------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // TODO: make safe
  def distinct[O: BsonValueDecoder](field: String): DistinctQuery[O] =
  DistinctQuery.from(underlying.distinct(field, classOf[BsonValue]))

  // TODO: make safe
  def distinct[F: BsonDocumentEncoder, O: BsonValueDecoder](field: String, filter: F): DistinctQuery[O] =
    DistinctQuery.from(underlying.distinct(field, BsonDocumentEncoder[F].encode(filter), classOf[BsonValue]))


  // - Indexes ---------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def createIndex[I: BsonDocumentEncoder](keys: I): MongoResult[String] =
    MongoResult(underlying.createIndex(BsonDocumentEncoder[I].encode(keys)))

  def createIndexWith[I: BsonDocumentEncoder](keys: I)(options: IndexOptions): MongoResult[String] =
    MongoResult(underlying.createIndex(BsonDocumentEncoder[I].encode(keys), options))

  // TODO: make safe
  def indexes[O: BsonDocumentDecoder](): IndexQuery[O] = IndexQuery.from(underlying.listIndexes(classOf[BsonDocument]))

  def dropIndex[I: BsonDocumentEncoder](keys: I): MongoResult[Unit] =
    MongoResult(underlying.dropIndex(BsonDocumentEncoder[I].encode(keys)))

  def dropIndex(name: String): MongoResult[Unit] = MongoResult(underlying.dropIndex(name))

  def dropIndexes(): MongoResult[Unit] = MongoResult(underlying.dropIndexes())



  // - Find ------------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // TODO: make safe
  def find[F: BsonDocumentEncoder](filter: F)(implicit da: BsonDocumentDecoder[A]): FindQuery[A] =
    FindQuery.from(underlying.find(BsonDocumentEncoder[F].encode(filter)))

  // TODO: make safe
  def find()(implicit da: BsonDocumentDecoder[A]): FindQuery[A] =
    FindQuery.from(underlying.find())



  // - Update ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // TODO: make safe
  private def findOneAndUpdate[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U,
                                                                               options: Option[FindOneAndUpdateOptions])
                                                                              (implicit da: BsonDocumentDecoder[A])
  : DecodeResult[A] =
  da.decode(options.fold(
    underlying.findOneAndUpdate(BsonDocumentEncoder[F].encode(filter), BsonDocumentEncoder[U].encode(update))
  )(o ⇒
    underlying.findOneAndUpdate(BsonDocumentEncoder[F].encode(filter), BsonDocumentEncoder[U].encode(update), o)
  ))


  // TODO: make safe
  def findOneAndUpdate[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)
                                                                      (implicit da: BsonDocumentDecoder[A])
  : DecodeResult[A] = findOneAndUpdate(filter, update, None)

  // TODO: make safe
  def findOneAndUpdateWith[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)
                                                                          (options: FindOneAndUpdateOptions)
                                                                          (implicit da: BsonDocumentDecoder[A])
  : DecodeResult[A] = findOneAndUpdate(filter, update, Some(options))

  def updateOne[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U): MongoResult[UpdateResult] =
    MongoResult(UpdateResult(underlying.updateOne(BsonDocumentEncoder[F].encode(filter),
      BsonDocumentEncoder[U].encode(update))))

  def updateOneWith[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)
                                                                   (options: UpdateOptions): MongoResult[UpdateResult] =
    MongoResult(UpdateResult(underlying.updateOne(BsonDocumentEncoder[F].encode(filter),
      BsonDocumentEncoder[U].encode(update), options)))

  def updateMany[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U): MongoResult[UpdateResult] =
    MongoResult(UpdateResult(underlying.updateMany(BsonDocumentEncoder[F].encode(filter),
      BsonDocumentEncoder[U].encode(update))))

  def updateManyWith[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)
                                                                    (options: UpdateOptions): MongoResult[UpdateResult] =
    MongoResult(UpdateResult(underlying.updateMany(BsonDocumentEncoder[F].encode(filter),
      BsonDocumentEncoder[U].encode(update), options)))


  // - Replacement -----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // TODO: make safe
  def findOneAndReplace[F: BsonDocumentEncoder](filter: F, replacement: A, options: Option[FindOneAndReplaceOptions])
                                               (implicit da: BsonDocumentDecoder[A], ea: BsonDocumentEncoder[A])
  : Option[DecodeResult[A]] =
  Option(options.fold(
    underlying.findOneAndReplace(BsonDocumentEncoder[F].encode(filter), ea.encode(replacement))
  )(o ⇒
    underlying.findOneAndReplace(BsonDocumentEncoder[F].encode(filter), ea.encode(replacement), o)
  )).map(da.decode)

  // TODO: make safe
  def findOneAndReplace[F: BsonDocumentEncoder](filter: F, replacement: A)
                                               (implicit da: BsonDocumentDecoder[A], ea: BsonDocumentEncoder[A])
  : Option[DecodeResult[A]] = findOneAndReplace(filter, replacement, None)

  // TODO: make safe
  def findOneAndReplaceWith[F: BsonDocumentEncoder](filter: F, replacement: A)(options: FindOneAndReplaceOptions)
                                                   (implicit da: BsonDocumentDecoder[A], ea: BsonDocumentEncoder[A])
  : Option[DecodeResult[A]] = findOneAndReplace(filter, replacement, Some(options))

  def replaceOne[F: BsonDocumentEncoder](filter: F, rep: A)(implicit ea: BsonDocumentEncoder[A]): MongoResult[UpdateResult] =
    MongoResult(UpdateResult(underlying.replaceOne(BsonDocumentEncoder[F].encode(filter), ea.encode(rep))))

  def replaceOneWith[F: BsonDocumentEncoder](filter: F, rep: A)(options: UpdateOptions)
                                            (implicit ea: BsonDocumentEncoder[A]): MongoResult[UpdateResult] =
    MongoResult(UpdateResult(underlying.replaceOne(BsonDocumentEncoder[F].encode(filter), ea.encode(rep), options)))



  // - Delete ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def deleteMany[F: BsonDocumentEncoder](filter: F): MongoResult[DeleteResult] =
    MongoResult(underlying.deleteMany(BsonDocumentEncoder[F].encode(filter)))

  def deleteManyWith[F: BsonDocumentEncoder](filter: F)(options: DeleteOptions): MongoResult[DeleteResult] =
    MongoResult(underlying.deleteMany(BsonDocumentEncoder[F].encode(filter), options))

  def deleteOne[F: BsonDocumentEncoder](filter: F): MongoResult[DeleteResult] =
    MongoResult(underlying.deleteOne(BsonDocumentEncoder[F].encode(filter)))

  def deleteOneWith[F: BsonDocumentEncoder](filter: F)(options: DeleteOptions): MongoResult[DeleteResult] =
    MongoResult(underlying.deleteOne(BsonDocumentEncoder[F].encode(filter), options))

  // TODO: make safe
  def findOneAndDelete[F: BsonDocumentEncoder](filter: F)(implicit da: BsonDocumentDecoder[A]): DecodeResult[A] =
    da.decode(underlying.findOneAndDelete(BsonDocumentEncoder[F].encode(filter)))




  // - Insert ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def insertMany(documents: A*)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    MongoResult(underlying.insertMany(documents.map(ea.encode).toList.asJava))

  def insertManyWith(documents: A*)(options: InsertManyOptions)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    MongoResult(underlying.insertMany(documents.map(ea.encode).toList.asJava, options))

  def insertOne(document: A)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    MongoResult(underlying.insertOne(ea.encode(document)))

  def insertOneWith(document: A)(options: InsertOneOptions)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    MongoResult(underlying.insertOne(ea.encode(document), options))



  // - Misc. -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  override def toString = s"MongoCollection($namespace)"

  def drop(): Unit = underlying.drop()

  def namespace: MongoNamespace = underlying.getNamespace

  def rename(db: String, name: String): MongoResult[Unit] =
    MongoResult(underlying.renameCollection(new MongoNamespace(db, name)))

  def renameWith(db: String, name: String)(options: RenameCollectionOptions): MongoResult[Unit] =
    MongoResult(underlying.renameCollection(new MongoNamespace(db, name), options))

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
