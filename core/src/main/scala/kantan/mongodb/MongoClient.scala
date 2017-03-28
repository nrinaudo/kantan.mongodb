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

class MongoClient private (private val client: MClient) extends Closeable {
  def database(name: String): MongoDatabase = new MongoDatabase(client.getDatabase(name))

  def databases(): Iterator[DatabaseInfo] =
  // We're assuming that DatabaseInfo is always succesfully decoded. Probably an ok assumption to make, but still...
    client.listDatabases(classOf[BsonDocument]).iterator().asScala.map(BsonDocumentDecoder[DatabaseInfo].unsafeDecode)

  def databaseNames(): Iterator[String] = databases().map(_.name)

  override def close() = client.close()
}

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
object MongoClient {
  // - Instance creation methods ---------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def local(creds: List[MongoCredential] = List.empty,
            options: Option[MongoClientOptions] = None): MongoClient =
    fromAddress(List(ServerAddress.default), List.empty, options)

  def fromUri(uri: String): Option[MongoClient] =
    Try(new MongoClientURI(uri, com.mongodb.MongoClientOptions.builder().codecRegistry(kantan.mongodb.io.registry)))
      .map(u ⇒ new MongoClient(new MClient(u, driverInfo)))
      .toOption

  def fromAddress(cluster: List[ServerAddress],
            creds: List[MongoCredential] = List.empty,
            options: Option[MongoClientOptions] = None): MongoClient = {
    new MongoClient(new MClient(cluster.map(_.legacy).asJava, creds.asJava, addRegistry(options), driverInfo))
  }



  // - Internal helpers ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  private def addRegistry(options: Option[MongoClientOptions]): MongoClientOptions =
    options.map(o ⇒ com.mongodb.MongoClientOptions.builder(o)).getOrElse(com.mongodb.MongoClientOptions.builder())
      .codecRegistry(kantan.mongodb.io.registry).build()

  def driverInfo: MongoDriverInformation = MongoDriverInformation.builder().driverName("kantan.mongodb")
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
