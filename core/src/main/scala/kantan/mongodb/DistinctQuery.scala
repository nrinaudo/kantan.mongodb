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
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import kantan.mongodb.DistinctQuery.Config
import kantan.mongodb.options.Collation
import scala.concurrent.duration.Duration

final class DistinctQuery[A] private (val config: Config, private val eval: Config ⇒ ResourceIterator[A])
    extends ResourceIterable[A] {
  override type Repr[X] = DistinctQuery[X]

  def withConfig(c: Config): DistinctQuery[A] = new DistinctQuery[A](c, eval)

  def batchSize(i: Int): DistinctQuery[A]           = withConfig(config.copy(batchSize = Some(i)))
  def collation(c: Collation): DistinctQuery[A]     = withConfig(config.copy(collation = Some(c)))
  def maxTime(duration: Duration): DistinctQuery[A] = withConfig(config.copy(maxTime = Some(duration)))

  override def iterator = eval(config)

  override protected def onIterator[B](f: ResourceIterator[A] ⇒ ResourceIterator[B]) =
    new DistinctQuery[B](config, eval andThen f)
}

object DistinctQuery {
  final case class Config(batchSize: Option[Int], collation: Option[Collation], maxTime: Option[Duration])

  object Config {
    val empty: Config = Config(None, None, None)
  }

  private[mongodb] def from[R: BsonValueDecoder](f: ⇒ DistinctIterable[BsonValue]): DistinctQuery[MongoResult[R]] =
    new DistinctQuery[MongoResult[R]](
      Config.empty,
      conf ⇒ {
        val iterable = f

        conf.batchSize.foreach(i ⇒ iterable.batchSize(i))
        conf.collation.foreach(c ⇒ iterable.collation(c.legacy))
        conf.maxTime.foreach(d ⇒ iterable.maxTime(d.length, d.unit))

        MongoIterator(iterable)
      }
    )
}
