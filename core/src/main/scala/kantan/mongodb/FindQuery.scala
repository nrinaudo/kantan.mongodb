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
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import kantan.mongodb.FindQuery.{Config, CursorType}
import kantan.mongodb.options.Collation
import scala.concurrent.duration.Duration

final class FindQuery[A] private (val config: Config, private val eval: Config ⇒ ResourceIterator[A])
  extends ResourceIterable[A] {
  override type Repr[X] = FindQuery[X]
  def withConfig(config: Config): FindQuery[A] = new FindQuery[A](config, eval)
  def batchSize(i: Int): FindQuery[A] = withConfig(config.copy(batchSize = Some(i)))
  def collation(c: Collation): FindQuery[A] = withConfig(config.copy(collation = Some(c)))
  def cursorType(c: CursorType): FindQuery[A] = withConfig(config.copy(cursorType= Some(c)))
  def limit(i: Int): FindQuery[A] = withConfig(config.copy(limit = Some(i)))
  def maxAwaitTime(duration: Duration): FindQuery[A] = withConfig(config.copy(maxAwaitTime= Some(duration)))
  def maxTime(duration: Duration): FindQuery[A] = withConfig(config.copy(maxTime = Some(duration)))
  def modifiers[E: BsonDocumentEncoder](e: E): FindQuery[A] =
    withConfig(config.copy(modifiers = Some(BsonDocumentEncoder[E].encode(e))))
  def noCursorTimeout(b: Boolean): FindQuery[A] = withConfig(config.copy(noCursorTimeout = Some(b)))
  def partial(b: Boolean): FindQuery[A] = withConfig(config.copy(partial = Some(b)))
  def projection[E: BsonDocumentEncoder](e: E): FindQuery[A] =
    withConfig(config.copy(projection = Some(BsonDocumentEncoder[E].encode(e))))
  def skip(i: Int): FindQuery[A] = withConfig(config.copy(skip = Some(i)))
  def sort[E: BsonDocumentEncoder](e: E): FindQuery[A] =
    withConfig(config.copy(sort = Some(BsonDocumentEncoder[E].encode(e))))

  override def take(n: Int) = limit(n)
  override def drop(n: Int) = skip(n)

  override def iterator = eval(config)
  override protected def onIterator[B](f: ResourceIterator[A] ⇒ ResourceIterator[B]) =
    new FindQuery[B](config, eval andThen f)
}

object FindQuery {
  final case class Config(batchSize: Option[Int], collation: Option[Collation], cursorType: Option[CursorType],
                          limit: Option[Int], maxAwaitTime: Option[Duration], maxTime: Option[Duration],
                          modifiers: Option[BsonDocument], noCursorTimeout: Option[Boolean], partial: Option[Boolean],
                          projection: Option[BsonDocument], skip: Option[Int], sort: Option[BsonDocument])

  object Config {
    val empty: Config = Config(None, None, None, None, None, None, None, None, None, None, None, None)
  }


  sealed abstract class CursorType(private[mongodb] val legacy: com.mongodb.CursorType)
    extends Product with Serializable
  object CursorType {
    case object NonTailable extends CursorType(com.mongodb.CursorType.NonTailable)
    case object Tailable extends CursorType(com.mongodb.CursorType.Tailable)
    case object TailableAwait extends CursorType(com.mongodb.CursorType.TailableAwait)
  }

  private[mongodb] def from[R: BsonDocumentDecoder](f: ⇒ FindIterable[BsonDocument]): FindQuery[MongoResult[R]] =
    new FindQuery[MongoResult[R]](Config.empty, conf ⇒ {
      val iterable = f

      conf.batchSize.foreach(iterable.batchSize)
      conf.collation.foreach(c ⇒ iterable.collation(c.legacy))
      conf.cursorType.foreach(c ⇒ iterable.cursorType(c.legacy))
      conf.limit.foreach(iterable.limit)
      conf.maxAwaitTime.foreach(d ⇒ iterable.maxAwaitTime(d.length, d.unit))
      conf.maxTime.foreach(d ⇒ iterable.maxTime(d.length, d.unit))
      conf.modifiers.foreach(iterable.modifiers)
      conf.noCursorTimeout.foreach(iterable.noCursorTimeout)
      conf.partial.foreach(iterable.partial)
      conf.projection.foreach(iterable.projection)
      conf.skip.foreach(iterable.skip)
      conf.sort.foreach(iterable.sort)

      MongoIterator(iterable)
    })
}
