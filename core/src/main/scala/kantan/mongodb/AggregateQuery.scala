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
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import kantan.mongodb.AggregateQuery.Config
import kantan.mongodb.options.Collation
import scala.concurrent.duration.Duration

final class AggregateQuery[A] private (val config: Config, private val eval: Config ⇒ ResourceIterator[A])
    extends ResourceIterable[A] {
  type Repr[X] = AggregateQuery[X]

  def withConfig(conf: Config): AggregateQuery[A] = new AggregateQuery[A](conf, eval)

  def allowDiskUse(b: Boolean): AggregateQuery[A]    = withConfig(config.copy(allowDiskUse = Some(b)))
  def batchSize(i: Int): AggregateQuery[A]           = withConfig(config.copy(batchSize = Some(i)))
  def collation(c: Collation): AggregateQuery[A]     = withConfig(config.copy(collation = Some(c)))
  def maxTime(duration: Duration): AggregateQuery[A] = withConfig(config.copy(maxTime = Some(duration)))

  override def iterator = eval(config)

  override protected def onIterator[B](f: ResourceIterator[A] ⇒ ResourceIterator[B]) =
    new AggregateQuery[B](config, eval andThen f)
}

object AggregateQuery {
  final case class Config(allowDiskUse: Option[Boolean],
                          batchSize: Option[Int],
                          collation: Option[Collation],
                          maxTime: Option[Duration])
  object Config {
    val empty: Config = Config(None, None, None, None)
  }

  private[mongodb] def from[R: BsonDocumentDecoder](
    f: ⇒ AggregateIterable[BsonDocument]
  ): AggregateQuery[MongoResult[R]] =
    new AggregateQuery[MongoResult[R]](
      Config.empty,
      conf ⇒ {
        val iterable = f

        conf.allowDiskUse.foreach(b ⇒ iterable.allowDiskUse(b))
        conf.batchSize.foreach(i ⇒ iterable.batchSize(i))
        conf.collation.foreach(c ⇒ iterable.collation(c.legacy))
        conf.maxTime.foreach(d ⇒ iterable.maxTime(d.length, d.unit))

        MongoIterator(iterable)
      }
    )
}
