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

import com.mongodb.client.ListDatabasesIterable
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import kantan.mongodb.DatabaseQuery.Config
import scala.concurrent.duration.Duration

final class DatabaseQuery[A](config: Config, eval: Config ⇒ ResourceIterator[A]) extends ResourceIterable[A] {
  type Repr[X] = DatabaseQuery[X]

  def withConfig(conf: Config): DatabaseQuery[A] = new DatabaseQuery[A](conf, eval)

  def batchSize(i: Int): DatabaseQuery[A] = withConfig(config.copy(batchSize = Some(i)))
  def maxTime(duration: Duration): DatabaseQuery[A] = withConfig(config.copy(maxTime = Some(duration)))

  override def iterator = eval(config)

  override protected def onIterator[B](f: ResourceIterator[A] ⇒ ResourceIterator[B]) =
  new DatabaseQuery[B](config, eval andThen f)
}

object DatabaseQuery {
  final case class Config(batchSize: Option[Int], maxTime: Option[Duration])

  object Config {
    val empty: Config = Config(None, None)
  }

  private[mongodb] def from[R: BsonDocumentDecoder](f: ⇒ ListDatabasesIterable[BsonDocument])
  : DatabaseQuery[MongoResult[R]] = new DatabaseQuery[MongoResult[R]](Config.empty, conf ⇒ {
    val iterable = f

    conf.batchSize.foreach(iterable.batchSize)

    MongoIterator(iterable)
  })
}