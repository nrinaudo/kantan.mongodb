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
import kantan.codecs.resource.ResourceIterable
import scala.concurrent.duration.Duration

abstract class FindQuery[A] extends ResourceIterable[A] {
  def batchSize(i: Int): FindQuery[A]
  def collation(c: Collation): FindQuery[A]
  def cursorType(c: CursorType): FindQuery[A]
  def limit(i: Int): FindQuery[A]
  def maxAwaitTime(l: Long, u: TimeUnit): FindQuery[A]
  def maxTime(l: Long, u: TimeUnit): FindQuery[A]
  def modifiers[E: BsonDocumentEncoder](e: E): FindQuery[A]
  def noCursorTimeout(b: Boolean): FindQuery[A]
  def partial(b: Boolean): FindQuery[A]
  def projection[E: BsonDocumentEncoder](e: E): FindQuery[A]
  def skip(i: Int): FindQuery[A]
  def sort[E: BsonDocumentEncoder](e: E): FindQuery[A]

  override final def toString = s"${getClass.getName}@${Integer.toHexString(hashCode())}"
}

private object FindQuery {
  private[mongodb] def from[R: BsonDocumentDecoder](f: ⇒ FindIterable[BsonDocument]): FindQuery[MongoResult[R]] =
    FindQueryImpl(None, None, None, None, None, None, None, None, None, None, None, None, () ⇒ f)

  private final case class FindQueryImpl[A: BsonDocumentDecoder](
                                                                  batchSize: Option[Int],
                                                                  collation: Option[Collation],
                                                                  cursor: Option[CursorType],
                                                                  lim: Option[Int],
                                                                  awaitTime: Option[Duration],
                                                                  time: Option[Duration],
                                                                  mods: Option[BsonDocument],
                                                                  noTimeout: Option[Boolean],
                                                                  part: Option[Boolean],
                                                                  proj: Option[BsonDocument],
                                                                  drop: Option[Int],
                                                                  srt: Option[BsonDocument],
                                                                  eval: () ⇒ FindIterable[BsonDocument]
                                                                ) extends FindQuery[MongoResult[A]] {
    override def batchSize(size: Int) = copy(batchSize = Some(size))
    override def collation(c: Collation) = copy(collation = Some(c))
    override def cursorType(c: CursorType) = copy(cursor = Some(c))
    override def limit(l: Int) = copy(lim = Some(l))
    override def maxAwaitTime(t: Long, unit: TimeUnit) = copy(awaitTime = Some(Duration(t, unit)))
    override def maxTime(t: Long, unit: TimeUnit) = copy(time = Some(Duration(t, unit)))
    override def modifiers[B: BsonDocumentEncoder](m: B) = copy(mods = Some(BsonDocumentEncoder[B].encode(m)))
    override def noCursorTimeout(n: Boolean) = copy(noTimeout = Some(n))
    override def partial(p: Boolean) = copy(part = Some(p))
    override def projection[B: BsonDocumentEncoder](b: B) = copy(proj = Some(BsonDocumentEncoder[B].encode(b)))
    override def skip(i: Int) = copy(drop = Some(i))
    override def sort[B: BsonDocumentEncoder](b: B) = copy(srt = Some(BsonDocumentEncoder[B].encode(b)))

    override def iterator = {
      val iterable = eval()

      batchSize.foreach(iterable.batchSize)
      collation.foreach(iterable.collation)
      cursor.foreach(iterable.cursorType)
      lim.foreach(iterable.limit)
      awaitTime.foreach(d ⇒ iterable.maxAwaitTime(d.length, d.unit))
      time.foreach(d ⇒ iterable.maxTime(d.length, d.unit))
      mods.foreach(iterable.modifiers)
      noTimeout.foreach(iterable.noCursorTimeout)
      part.foreach(iterable.partial)
      proj.foreach(iterable.projection)
      drop.foreach(iterable.skip)
      srt.foreach(iterable.sort)

      MongoIterator(iterable)
    }
  }
}
