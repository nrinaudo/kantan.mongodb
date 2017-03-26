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

import com.mongodb.client.AggregateIterable
import kantan.codecs.resource.ResourceIterable
import scala.concurrent.duration.{Duration, TimeUnit}

sealed abstract class AggregateQuery[A] extends ResourceIterable[A] {
  def allowDiskUse(b: Boolean): AggregateQuery[A]
  def batchSize(i: Int): AggregateQuery[A]
  def bypassDocumentValidation(b: Boolean): AggregateQuery[A]
  def collation(c: Collation): AggregateQuery[A]
  def maxTime(l: Long, t: TimeUnit): AggregateQuery[A]
  def useCursor(b: Boolean): AggregateQuery[A]

  override final def toString = s"${getClass.getName}@${Integer.toHexString(hashCode())}"
}

private object AggregateQuery {
  private[mongodb] def from[R: BsonDocumentDecoder](f: ⇒ AggregateIterable[BsonDocument])
  : AggregateQuery[MongoResult[R]] = AggregateQueryImpl(None, None, None, None, None, None, () ⇒ f)

  private final case class AggregateQueryImpl[A: BsonDocumentDecoder](
                                                                       diskUse: Option[Boolean],
                                                                       batchSize: Option[Int],
                                                                       bypassValidation: Option[Boolean],
                                                                       col: Option[Collation],
                                                                       time: Option[Duration],
                                                                       cursor: Option[Boolean],
                                                                       eval: () ⇒ AggregateIterable[BsonDocument]
                                                                     ) extends AggregateQuery[MongoResult[A]] {
    override def allowDiskUse(b: Boolean) = copy(diskUse = Some(b))
    override def batchSize(i: Int) = copy(batchSize = Some(i))
    override def bypassDocumentValidation(b: Boolean) = copy(bypassValidation = Some(b))
    override def collation(c: Collation) = copy(col = Some(c))
    override def maxTime(l: Long, t: TimeUnit) = copy(time = Some(Duration(l, t)))
    override def useCursor(b: Boolean) = copy(cursor = Some(b))

    override def iterator = {
      val iterable = eval()

      diskUse.foreach(b ⇒ iterable.allowDiskUse(b))
      batchSize.foreach(i ⇒ iterable.batchSize(i))
      bypassValidation.foreach(b ⇒ iterable.bypassDocumentValidation(b))
      col.foreach(c ⇒ iterable.collation(c))
      time.foreach(d ⇒ iterable.maxTime(d.length, d.unit))
      cursor.foreach(b ⇒ iterable.useCursor(b))

      MongoIterator(iterable)
    }
  }
}
