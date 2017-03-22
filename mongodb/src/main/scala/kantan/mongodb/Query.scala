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

import com.mongodb.client.FindIterable
import kantan.bson.{BsonDocument, BsonDocumentDecoder, BsonDocumentEncoder, DecodeResult}
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import scala.collection.JavaConverters._

class Query[A: BsonDocumentDecoder] private (private val batchSize: Option[Int],
                                             private val collation: Option[Collation],
                                             private val cursorType: Option[CursorType],
                                             private val limit: Option[Int],
                                             // await time
                                             // time
                                             private val modifiers: Option[BsonDocument],
                                             private val noCursorTimeout: Option[Boolean],
                                             private val partial: Option[Boolean],
                                             private val projection: Option[BsonDocument],
                                             private val skip: Option[Int],
                                             private val sort: Option[BsonDocument],
                                             private val query: () ⇒ FindIterable[BsonDocument]
                                            ) extends ResourceIterable[DecodeResult[A]] {
  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  private def copy(batchSize: Option[Int] = batchSize, collation: Option[Collation] = collation,
                   cursorType: Option[CursorType] = cursorType, limit: Option[Int] = limit,
                   modifiers: Option[BsonDocument] = modifiers, noCursorTimeout: Option[Boolean] = noCursorTimeout,
                   partial: Option[Boolean] = partial, projection: Option[BsonDocument] = projection,
                   skip: Option[Int] = skip, sort: Option[BsonDocument] = sort,
                   query: () ⇒ FindIterable[BsonDocument] = query): Query[A] =
    new Query(batchSize, collation, cursorType, limit, modifiers, noCursorTimeout, partial, projection, skip, sort,
      query)

  def batchSize(size: Int): Query[A] = copy(batchSize = Some(size))
  def collation(c: Collation): Query[A] = copy(collation = Some(c))
  def cursorType(c: CursorType): Query[A] = copy(cursorType = Some(c))
  def limit(l: Int): Query[A] = copy(limit = Some(l))
  def modifiers[B: BsonDocumentEncoder](m: B): Query[A] = copy(modifiers = Some(BsonDocumentEncoder[B].encode(m)))
  def noCursorTimeout(n: Boolean): Query[A] = copy(noCursorTimeout = Some(n))
  def partial(p: Boolean): Query[A] = copy(partial = Some(p))
  def projection[B: BsonDocumentEncoder](b: B): Query[A] = copy(projection = Some(BsonDocumentEncoder[B].encode(b)))
  def skip(i: Int): Query[A] = copy(skip = Some(i))
  def sort[B: BsonDocumentEncoder](b: B): Query[A] = copy(sort = Some(BsonDocumentEncoder[B].encode(b)))

  override def iterator = {
    val iterable = query()
    batchSize.foreach(iterable.batchSize)
    collation.foreach(iterable.collation)
    cursorType.foreach(iterable.cursorType)
    limit.foreach(iterable.limit)
    modifiers.foreach(iterable.modifiers)
    noCursorTimeout.foreach(iterable.noCursorTimeout)
    partial.foreach(iterable.partial)
    projection.foreach(iterable.projection)
    skip.foreach(iterable.skip)
    sort.foreach(iterable.sort)

    ResourceIterator.fromIterator(iterable.iterator().asScala.map(BsonDocumentDecoder[A].decode))
  }

  override def toString = s"${getClass.getName}@${Integer.toHexString(hashCode())}"
}

object Query {
  def apply[I: BsonDocumentEncoder, O: BsonDocumentDecoder](col: MongoCollection, filter: I): Query[O] =
    Query.from(() ⇒ col.underlying.find(BsonDocumentEncoder[I].encode(filter)))

  def apply[O: BsonDocumentDecoder](col: MongoCollection): Query[O] = Query.from(() ⇒ col.underlying.find)

  private def from[O: BsonDocumentDecoder](f: () ⇒ FindIterable[BsonDocument]): Query[O] =
    new Query[O](None, None, None, None, None, None, None, None, None, None, f)
}
