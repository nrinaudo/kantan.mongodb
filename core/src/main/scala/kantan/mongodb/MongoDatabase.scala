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

import com.mongodb.client.{MongoDatabase ⇒ MDatabase}
import kantan.mongodb.MongoDatabase.CollectionInfo
import scala.collection.JavaConverters._

class MongoDatabase private[mongodb] (val underlying: MDatabase) {
  def name: String = underlying.getName
  override def toString = s"MongoDatabase($name)"
  def drop(): Unit = underlying.drop()



  // - Collection creation ---------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Creates a new collection with the specified name. */
  def createCollection[A](name: String): MongoCollection[A] = {
    underlying.createCollection(name)
    collection(name)
  }

  /** Creates a new collection with the specified name, using the specified options. */
  def createCollectionWith[A](name: String)(options: CreateCollectionOptions): MongoCollection[A] = {
    underlying.createCollection(name, options)
    collection(name)
  }



  // - View creation ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  private def createView[I: BsonDocumentEncoder, A](name: String, on: String, options: Option[CreateViewOptions],
                                                    pipeline: Seq[I]): MongoCollection[A] = {
    val p = pipeline.map(BsonDocumentEncoder[I].encode).toList.asJava
    options.fold(underlying.createView(name, on, p))(o ⇒ underlying.createView(name, on, p, o))
    collection(name)
  }

  def createView[I: BsonDocumentEncoder, A](name: String, on: String, pipeline: I*): MongoCollection[A] =
    createView(name, on, None, pipeline)

  def createViewWith[I: BsonDocumentEncoder, A](name: String, on: String, pipeline: I*)
                                               (options: CreateViewOptions): MongoCollection[A] =
    createView(name, on, Some(options), pipeline)



  // - Collection retrieval --------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def collections(): Iterator[CollectionInfo] = underlying.listCollections(classOf[BsonDocument]).iterator().asScala
    .map(BsonDocumentDecoder[CollectionInfo].unsafeDecode)

  def collectionNames(): Iterator[String] = collections().map(_.name)

  def collection[A](name: String): MongoCollection[A] =
    new MongoCollection(underlying.getCollection(name, classOf[BsonDocument]))



  // - Command execution -----------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  private def runCommand[I: BsonDocumentEncoder, O: BsonDocumentDecoder]
  (command: I, pref: Option[ReadPreference]): DecodeResult[O] =
  BsonDocumentDecoder[O].decode(underlying.runCommand(BsonDocumentEncoder[I].encode(command),
    pref.getOrElse(com.mongodb.ReadPreference.primary()), classOf[BsonDocument]))

  def runCommand[I: BsonDocumentEncoder, O: BsonDocumentDecoder](command: I): DecodeResult[O] =
    runCommand(command, None)

  def runCommandWith[I: BsonDocumentEncoder, O: BsonDocumentDecoder](command: I)(p: ReadPreference): DecodeResult[O] =
    runCommand(command, Some(p))



  // - Configuration ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def readConcern: ReadConcern = underlying.getReadConcern
  def readPreference: ReadPreference = underlying.getReadPreference
  def writeConcern: WriteConcern = underlying.getWriteConcern
  def withReadConcern(concern: ReadConcern): MongoDatabase = new MongoDatabase(underlying.withReadConcern(concern))
  def withReadPreference(pref: ReadPreference): MongoDatabase = new MongoDatabase(underlying.withReadPreference(pref))
  def withWriteConcern(concern: WriteConcern): MongoDatabase = new MongoDatabase(underlying.withWriteConcern(concern))
}

object MongoDatabase {
  // TODO: add support for options, once I've found a complete list.
  final case class CollectionInfo(name: String)

  implicit val infoDecoder: BsonDocumentDecoder[CollectionInfo] =
    BsonDocumentDecoder.decoder("name")(CollectionInfo.apply _)
}
