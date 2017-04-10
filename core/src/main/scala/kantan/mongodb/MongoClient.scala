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

import com.mongodb.{MongoClient ⇒ MClient}
import com.mongodb.client.MongoDriverInformation
import java.io.Closeable
import kantan.mongodb.MongoClient.DatabaseInfo
import scala.collection.JavaConverters._
import scala.util.Try

// TODO: databases() need to be turned into something more generic && safe.

class MongoClient private (private val client: MClient) extends Closeable {
  /** Returns the database with the specified name. */
  def database(name: String): MongoDatabase = new MongoDatabase(client.getDatabase(name))

  def databases(): Iterator[DatabaseInfo] =
  // We're assuming that DatabaseInfo is always succesfully decoded. Probably an ok assumption to make, but still...
    client.listDatabases(classOf[BsonDocument]).iterator().asScala.map(BsonDocumentDecoder[DatabaseInfo].unsafeDecode)

  def databaseNames(): Iterator[String] = databases().map(_.name)

  override def toString = client.getAllAddress.asScala.mkString("MongoClient(", ", ", ")")

  override def close() = client.close()
}

/** Provides functions for connecting to a MongoDB server or cluster.
  *
  * There are three main ways to connect to a MongoDB cluster:
  *
  *  - the [[local]] one (which basically attempts to connect to [[ServerAddress.default]].
  *  - a [[fromAddress list of server addresses]].
  *  - a [[fromUri MongoDB URI]] describing a server cluster.
  *
  * The first two come with a `xyzWith` variant that lets you pass in options at connection time.
  */
object MongoClient {
  // - Instance creation methods ---------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  private def defaultOptions: MongoClientOptions =
    com.mongodb.MongoClientOptions.builder().build()

  /** Connects to the local MongoDB server, using the specified credentials.
    *
    * Specify an empty list for anonymous login.
    */
  def local(creds: List[MongoCredential]): MongoClient =
    fromAddressWith(List(ServerAddress.default), creds)(defaultOptions)

  /** Connects to the local MongoDB server, using the specified credentials and options.
    *
    * Specify an empty list for anonymous login.
    */
  def localWith(creds: List[MongoCredential])(options: MongoClientOptions): MongoClient =
    fromAddressWith(List(ServerAddress.default), creds)(options)

  /** Connects to the specified MongoDB cluster, using the specified credentials.
    *
    * Specify an empty list for anonymous login.
    */
  def fromAddress(cluster: List[ServerAddress], creds: List[MongoCredential]): MongoClient =
    fromAddressWith(cluster, creds)(defaultOptions)

  /** Connects to the specified MongoDB cluster, using the specified credentials and options.
    *
    * Specify an empty list for anonymous login.
    */
  def fromAddressWith(cluster: List[ServerAddress], creds: List[MongoCredential])
                     (options: MongoClientOptions): MongoClient =
      new MongoClient(new MClient(cluster.map(_.legacy).asJava, creds.asJava,
        com.mongodb.MongoClientOptions.builder(options).codecRegistry(kantan.mongodb.io.registry).build(), driverInfo))

  /** Connects to the specified MongoDB cluster. */
  def fromUri(uri: String): Option[MongoClient] =
    Try(new MongoClientURI(uri, com.mongodb.MongoClientOptions.builder().codecRegistry(kantan.mongodb.io.registry)))
      .map(u ⇒ new MongoClient(new MClient(u, driverInfo)))
      .toOption


  private val driverInfo: MongoDriverInformation = MongoDriverInformation.builder().driverName("kantan.mongodb")
    .driverVersion(BuildInfo.version)
    .driverPlatform("Scala " + scala.util.Properties.scalaPropOrElse("version.number", "unknown")).build()



  // - Database info ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class DatabaseInfo(name: String, sizeOnDisk: Double, empty: Boolean)

  object DatabaseInfo {
    implicit val infoDecoder: BsonDocumentDecoder[DatabaseInfo] =
      BsonDocumentDecoder.decoder("name", "sizeOnDisk", "empty")(DatabaseInfo.apply _)
  }
}
