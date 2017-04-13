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
import kantan.mongodb.IndexQuery.Config
import scala.concurrent.duration.Duration

final class IndexQuery[A] private(val config: Config, eval: Config ⇒ ResourceIterator[A]) extends ResourceIterable[A] {
  override type Repr[X] = IndexQuery[X]

  def withConfig(conf: Config): IndexQuery[A] = new IndexQuery[A](conf, eval)

  def batchSize(i: Int): IndexQuery[A] = withConfig(config.copy(batchSize = Some(i)))
  def maxTime(duration: Duration): IndexQuery[A] = withConfig(config.copy(maxTime = Some(duration)))

  override def iterator = eval(config)

  override protected def onIterator[B](f: ResourceIterator[A] ⇒ ResourceIterator[B]) =
    new IndexQuery[B](config, eval andThen f)
}

object IndexQuery {
  final case class Config(batchSize: Option[Int], maxTime: Option[Duration])

  object Config {
    val empty: Config = Config(None, None)
  }


  private[mongodb] def from[R: BsonDocumentDecoder](f: ⇒ ListIndexesIterable[BsonDocument])
  : IndexQuery[MongoResult[R]] = new IndexQuery[MongoResult[R]](Config.empty, conf ⇒ {
    val iterable = f

    conf.batchSize.foreach(i ⇒ iterable.batchSize(i))
    conf.maxTime.foreach(d ⇒ iterable.maxTime(d.length, d.unit))

    MongoIterator(iterable)
  })
}
