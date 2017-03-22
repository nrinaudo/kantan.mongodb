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
import java.util.concurrent.TimeUnit
import kantan.bson.{BsonDocument, BsonDocumentDecoder, BsonDocumentEncoder, DecodeResult}
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

class FindQuery[A: BsonDocumentDecoder] private(private val batchSize: Option[Int],
                                                private val collation: Option[Collation],
                                                private val cursorType: Option[CursorType],
                                                private val limit: Option[Int],
                                                private val maxAwaitTime: Option[Duration],
                                                private val maxTime: Option[Duration],
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
                   maxAwaitTime: Option[Duration] = maxAwaitTime, maxTime: Option[Duration] = maxTime,
                   modifiers: Option[BsonDocument] = modifiers, noCursorTimeout: Option[Boolean] = noCursorTimeout,
                   partial: Option[Boolean] = partial, projection: Option[BsonDocument] = projection,
                   skip: Option[Int] = skip, sort: Option[BsonDocument] = sort,
                   query: () ⇒ FindIterable[BsonDocument] = query): FindQuery[A] =
    new FindQuery(batchSize, collation, cursorType, limit, maxAwaitTime, maxTime, modifiers, noCursorTimeout, partial,
      projection, skip, sort, query)

  def batchSize(size: Int): FindQuery[A] = copy(batchSize = Some(size))
  def collation(c: Collation): FindQuery[A] = copy(collation = Some(c))
  def cursorType(c: CursorType): FindQuery[A] = copy(cursorType = Some(c))
  def limit(l: Int): FindQuery[A] = copy(limit = Some(l))
  def maxAwaitTime(t: Long, unit: TimeUnit): FindQuery[A] = copy(maxAwaitTime = Some(Duration(t, unit)))
  def maxTime(t: Long, unit: TimeUnit): FindQuery[A] = copy(maxTime = Some(Duration(t, unit)))
  def modifiers[B: BsonDocumentEncoder](m: B): FindQuery[A] = copy(modifiers = Some(BsonDocumentEncoder[B].encode(m)))
  def noCursorTimeout(n: Boolean): FindQuery[A] = copy(noCursorTimeout = Some(n))
  def partial(p: Boolean): FindQuery[A] = copy(partial = Some(p))
  def projection[B: BsonDocumentEncoder](b: B): FindQuery[A] = copy(projection = Some(BsonDocumentEncoder[B].encode(b)))
  def skip(i: Int): FindQuery[A] = copy(skip = Some(i))
  def sort[B: BsonDocumentEncoder](b: B): FindQuery[A] = copy(sort = Some(BsonDocumentEncoder[B].encode(b)))

  override def iterator = {
    val iterable = query()
    batchSize.foreach(iterable.batchSize)
    collation.foreach(iterable.collation)
    cursorType.foreach(iterable.cursorType)
    limit.foreach(iterable.limit)
    maxAwaitTime.foreach(d ⇒ iterable.maxAwaitTime(d.length, d.unit))
    maxTime.foreach(d ⇒ iterable.maxTime(d.length, d.unit))
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

object FindQuery {
  def apply[F: BsonDocumentEncoder, A: BsonDocumentDecoder](col: MongoCollection[A], filter: F): FindQuery[A] =
    FindQuery.from(() ⇒ col.underlying.find(BsonDocumentEncoder[F].encode(filter)))

  def apply[A: BsonDocumentDecoder](col: MongoCollection[A]): FindQuery[A] = FindQuery.from(() ⇒ col.underlying.find)

  private def from[O: BsonDocumentDecoder](f: () ⇒ FindIterable[BsonDocument]): FindQuery[O] =
    new FindQuery[O](None, None, None, None, None, None, None, None, None, None, None, None, f)
}
