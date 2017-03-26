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

import com.mongodb.client.ListIndexesIterable
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, TimeUnit}

sealed abstract class IndexQuery[A] extends ResourceIterable[A] {
  def batchSize(i: Int): IndexQuery[A]
  def maxTime(l: Long, u: TimeUnit): IndexQuery[A]
}

private object IndexQuery {
  private[mongodb] def from[R: BsonDocumentDecoder](f: ⇒ ListIndexesIterable[BsonDocument])
  : IndexQuery[MongoResult[R]] = IndexQueryImpl(None, None, () ⇒ f)

  private final case class IndexQueryImpl[A: BsonDocumentDecoder](
                                                                 batch: Option[Int],
                                                                 time: Option[Duration],
                                                                 eval: () ⇒ ListIndexesIterable[BsonDocument]
                                                                 ) extends IndexQuery[MongoResult[A]] {
    override def batchSize(i: Int) = copy(batch = Some(i))
    override def maxTime(l: Long, u: TimeUnit) = copy(time = Some(Duration(l, u)))
    override def iterator = {
      val iterable = eval()

      batch.foreach(i ⇒ iterable.batchSize(i))
      time.foreach(d ⇒ iterable.maxTime(d.length, d.unit))

      ResourceIterator.fromIterator(iterable.iterator().asScala.map(BsonDocumentDecoder[A].decode))
    }

    override def toString = s"${getClass.getName}@${Integer.toHexString(hashCode())}"
  }
}
