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

import com.mongodb.client.{MongoCollection => MCollection}
import kantan.bson._
import scala.collection.JavaConverters._

class MongoCollection private[mongodb] (private val col: MCollection[BsonDocument]) {
  def find[I: BsonDocumentEncoder, O: BsonDocumentDecoder](filter: I): Iterator[DecodeResult[O]] = {
    col.find(BsonDocumentEncoder[I].encode(filter)).iterator().asScala.map(BsonDocumentDecoder[O].decode)
  }

  def find[O: BsonDocumentDecoder](): Iterator[DecodeResult[O]] =
    col.find().iterator().asScala.map(BsonDocumentDecoder[O].decode)

  override def toString = s"MongoCollection(${col.getNamespace})"
}
