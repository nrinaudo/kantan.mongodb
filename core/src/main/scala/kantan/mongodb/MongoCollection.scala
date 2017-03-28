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
import kantan.mongodb.options._
import scala.collection.JavaConverters._

class MongoCollection[A] private[mongodb] (private val underlying: MCollection[BsonDocument]) {
  // - Count -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def count(): MongoResult[Long] = MongoResult(underlying.count())
  def count[I: BsonDocumentEncoder](filter: I): MongoResult[Long] =
    countWith(filter)(CountOpts.default)
  def countWith[I: BsonDocumentEncoder](filter: I)(options: CountOpts): MongoResult[Long] =
    MongoResult(underlying.count(BsonDocumentEncoder[I].encode(filter), options.legacy))



  // - Bulk operations -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def bulkWrite(operations: BulkOperation*): MongoResult[BulkResult] =
    MongoResult(BulkResult(underlying.bulkWrite(operations.map(_.toModel).asJava)))
  def bulkWriteWith(operations: BulkOperation*)(options: BulkWriteOpts): MongoResult[BulkResult] =
    MongoResult(BulkResult(underlying.bulkWrite(operations.map(_.toModel).asJava, options.legacy)))


  // - Aggregate / Map-Reduce ------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def aggregate[F: BsonDocumentEncoder, O: BsonDocumentDecoder](filters: F*): AggregateQuery[MongoResult[O]] =
    AggregateQuery.from(underlying.aggregate(filters.map(BsonDocumentEncoder[F].encode).asJava))

  def mapReduce[O: BsonDocumentDecoder](map: String, reduce: String): MapReduceQuery[MongoResult[O]] =
    MapReduceQuery.from(underlying.mapReduce(map, reduce))



  // - Distinct --------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def distinct[O: BsonValueDecoder](field: String): DistinctQuery[MongoResult[O]] =
  DistinctQuery.from(underlying.distinct(field, classOf[BsonValue]))

  def distinct[F: BsonDocumentEncoder, O: BsonValueDecoder](field: String, filter: F): DistinctQuery[MongoResult[O]] =
    DistinctQuery.from(underlying.distinct(field, BsonDocumentEncoder[F].encode(filter), classOf[BsonValue]))


  // - Indexes ---------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def createIndex[I: BsonDocumentEncoder](keys: I): MongoResult[String] =
    createIndexWith(keys)(IndexOpts.default)

  def createIndexWith[I: BsonDocumentEncoder](keys: I)(options: IndexOpts): MongoResult[String] =
    MongoResult(underlying.createIndex(BsonDocumentEncoder[I].encode(keys), options.legacy))

  def indexes[O: BsonDocumentDecoder](): IndexQuery[MongoResult[O]] =
    IndexQuery.from(underlying.listIndexes(classOf[BsonDocument]))

  def dropIndex[I: BsonDocumentEncoder](keys: I): MongoResult[Unit] =
    MongoResult(underlying.dropIndex(BsonDocumentEncoder[I].encode(keys)))

  def dropIndex(name: String): MongoResult[Unit] = MongoResult(underlying.dropIndex(name))

  def dropIndexes(): MongoResult[Unit] = MongoResult(underlying.dropIndexes())



  // - Find ------------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def find[F: BsonDocumentEncoder](filter: F)(implicit da: BsonDocumentDecoder[A]): FindQuery[MongoResult[A]] =
    FindQuery.from(underlying.find(BsonDocumentEncoder[F].encode(filter)))

  def find()(implicit da: BsonDocumentDecoder[A]): FindQuery[MongoResult[A]] =
    FindQuery.from(underlying.find())



  // - Update ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def findOneAndUpdate[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)
                                                                      (implicit da: BsonDocumentDecoder[A])
  : MongoResult[A] = findOneAndUpdateWith(filter, update)(FindOneAndUpdateOpts.default)

  def findOneAndUpdateWith[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)
                                                                          (options: FindOneAndUpdateOpts)
                                                                          (implicit da: BsonDocumentDecoder[A])
  : MongoResult[A] = da.decode(underlying.findOneAndUpdate(BsonDocumentEncoder[F].encode(filter),
    BsonDocumentEncoder[U].encode(update), options.legacy))

  def updateOne[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U): MongoResult[UpdateResult] =
    updateOneWith(filter, update)(UpdateOpts.default)

  def updateOneWith[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)(options: UpdateOpts)
  : MongoResult[UpdateResult] = MongoResult(UpdateResult(underlying.updateOne(BsonDocumentEncoder[F].encode(filter),
    BsonDocumentEncoder[U].encode(update), options.legacy)))

  def updateMany[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U): MongoResult[UpdateResult] =
    updateManyWith(filter, update)(UpdateOpts.default)

  def updateManyWith[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F, update: U)(options: UpdateOpts)
  : MongoResult[UpdateResult] = MongoResult(UpdateResult(underlying.updateMany(BsonDocumentEncoder[F].encode(filter),
    BsonDocumentEncoder[U].encode(update), options.legacy)))


  // - Replacement -----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def findOneAndReplace[F: BsonDocumentEncoder](filter: F, replacement: A)
                                               (implicit da: BsonDocumentDecoder[A], ea: BsonDocumentEncoder[A])
  : Option[MongoResult[A]] = findOneAndReplaceWith(filter, replacement)(FindOneAndReplaceOpts.default)

  def findOneAndReplaceWith[F: BsonDocumentEncoder](filter: F, replacement: A)(options: FindOneAndReplaceOpts)
                                                   (implicit da: BsonDocumentDecoder[A], ea: BsonDocumentEncoder[A])
  : Option[MongoResult[A]] =
    Option(underlying.findOneAndReplace(BsonDocumentEncoder[F].encode(filter), ea.encode(replacement),
      options.legacy)).map(da.decode)

  def replaceOne[F: BsonDocumentEncoder](filter: F, rep: A)(implicit ea: BsonDocumentEncoder[A])
  : MongoResult[UpdateResult] = replaceOneWith(filter, rep)(UpdateOpts.default)

  def replaceOneWith[F: BsonDocumentEncoder](filter: F, rep: A)(options: UpdateOpts)
                                            (implicit ea: BsonDocumentEncoder[A]): MongoResult[UpdateResult] =
    MongoResult(UpdateResult(underlying.replaceOne(BsonDocumentEncoder[F].encode(filter), ea.encode(rep),
      options.legacy)))



  // - Delete ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def deleteMany[F: BsonDocumentEncoder](filter: F): MongoResult[DeleteResult] =
    deleteManyWith(filter)(DeleteOpts.default)

  def deleteManyWith[F: BsonDocumentEncoder](filter: F)(options: DeleteOpts): MongoResult[DeleteResult] =
    MongoResult(underlying.deleteMany(BsonDocumentEncoder[F].encode(filter), options.legacy))

  def deleteOne[F: BsonDocumentEncoder](filter: F): MongoResult[DeleteResult] =
    deleteOneWith(filter)(DeleteOpts.default)

  def deleteOneWith[F: BsonDocumentEncoder](filter: F)(options: DeleteOpts): MongoResult[DeleteResult] =
    MongoResult(underlying.deleteOne(BsonDocumentEncoder[F].encode(filter), options.legacy))

  def findOneAndDelete[F: BsonDocumentEncoder](filter: F)(implicit da: BsonDocumentDecoder[A]): MongoResult[A] =
    findOneAndDeleteWith(filter)(FindOneAndDeleteOpts.default)

  def findOneAndDeleteWith[F: BsonDocumentEncoder](filter: F)(options: FindOneAndDeleteOpts)
                                                  (implicit da: BsonDocumentDecoder[A]): MongoResult[A] =
      da.decode(underlying.findOneAndDelete(BsonDocumentEncoder[F].encode(filter), options.legacy))




  // - Insert ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def insertMany(documents: A*)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    insertManyWith(documents:_*)(InsertManyOpts.default)

  def insertManyWith(documents: A*)(options: InsertManyOpts)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    MongoResult(underlying.insertMany(documents.map(ea.encode).toList.asJava, options.legacy))

  def insertOne(document: A)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    insertOneWith(document)(InsertOneOpts.default)

  def insertOneWith(document: A)(options: InsertOneOpts)(implicit ea: BsonDocumentEncoder[A]): MongoResult[Unit] =
    MongoResult(underlying.insertOne(ea.encode(document), options.legacy))



  // - Misc. -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  override def toString = s"MongoCollection($namespace)"

  def drop(): Unit = underlying.drop()

  def namespace: MongoNamespace = MongoNamespace.fromLegacy(underlying.getNamespace)

  def rename(namespace: MongoNamespace): MongoResult[Unit] = renameWith(namespace)(RenameCollectionOpts.default)

  def renameWith(namespace: MongoNamespace)(options: RenameCollectionOpts): MongoResult[Unit] =
    MongoResult(underlying.renameCollection(namespace.legacy, options.legacy))

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
