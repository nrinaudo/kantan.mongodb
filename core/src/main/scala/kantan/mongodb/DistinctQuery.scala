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

import com.mongodb.client.DistinctIterable
import kantan.codecs.resource.ResourceIterable
import kantan.mongodb.options.Collation
import scala.concurrent.duration.Duration

abstract class DistinctQuery[A] extends ResourceIterable[A] {
  def batchSize(i: Int): DistinctQuery[A]
  def collation(c: Collation): DistinctQuery[A]
  def maxTime(duration: Duration): DistinctQuery[A]

  override final def toString = s"${getClass.getName}@${Integer.toHexString(hashCode())}"
}

private object DistinctQuery {
  private[mongodb] def from[R: BsonValueDecoder](f: ⇒ DistinctIterable[BsonValue]): DistinctQuery[MongoResult[R]] =
    DistinctQueryImpl(None, None, None, () ⇒ f)
  private final case class DistinctQueryImpl[A: BsonValueDecoder](
                                                                   batch: Option[Int],
                                                                   col: Option[Collation],
                                                                   time: Option[Duration],
                                                                   eval: () ⇒ DistinctIterable[BsonValue]
                                                                 ) extends DistinctQuery[MongoResult[A]] {
    override def batchSize(i: Int) = copy(batch = Some(i))
    override def collation(c: Collation) = copy(col = Some(c))
    override def maxTime(d: Duration) = copy(time = Some(d))
    override def iterator = {
      val iterable = eval()

      batch.foreach(i ⇒ iterable.batchSize(i))
      col.foreach(c ⇒ iterable.collation(c.legacy))
      time.foreach(d ⇒ iterable.maxTime(d.length, d.unit))

      MongoIterator(iterable)
    }
  }
}