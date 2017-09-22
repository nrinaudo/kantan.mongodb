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

import com.mongodb.client.MapReduceIterable
import com.mongodb.client.model.MapReduceAction
import kantan.codecs.resource.{ResourceIterable, ResourceIterator}
import kantan.mongodb.MapReduceQuery.{Action, Config}
import kantan.mongodb.options.Collation
import scala.concurrent.duration.Duration

final class MapReduceQuery[A] private (val config: Config, private val eval: Config ⇒ ResourceIterator[A])
    extends ResourceIterable[A] {
  override type Repr[X] = MapReduceQuery[X]

  def withConfig(conf: Config): MapReduceQuery[A] = new MapReduceQuery[A](conf, eval)

  def action(a: Action): MapReduceQuery[A]         = withConfig(config.copy(action = Some(a)))
  def batchSize(i: Int): MapReduceQuery[A]         = withConfig(config.copy(batchSize = Some(i)))
  def collectionName(n: String): MapReduceQuery[A] = withConfig(config.copy(collectionName = Some(n)))
  def databaseName(n: String): MapReduceQuery[A]   = withConfig(config.copy(databaseName = Some(n)))
  def filter[F: BsonDocumentEncoder](filter: F): MapReduceQuery[A] =
    withConfig(config.copy(filter = Some(BsonDocumentEncoder[F].encode(filter))))
  def finalizeFunction(s: String): MapReduceQuery[A] = withConfig(config.copy(finalizeFunction = Some(s)))
  def jsMode(m: Boolean): MapReduceQuery[A]          = withConfig(config.copy(jsMode = Some(m)))
  def nonAtomic(b: Boolean): MapReduceQuery[A]       = withConfig(config.copy(nonAtomic = Some(b)))
  def scope[S: BsonDocumentEncoder](scope: S): MapReduceQuery[A] =
    withConfig(config.copy(scope = Some(BsonDocumentEncoder[S].encode(scope))))
  def sharded(s: Boolean): MapReduceQuery[A] = withConfig(config.copy(sharded = Some(s)))
  def sort[S: BsonDocumentEncoder](sort: S): MapReduceQuery[A] =
    withConfig(config.copy(sort = Some(BsonDocumentEncoder[S].encode(sort))))
  def collation(c: Collation): MapReduceQuery[A]     = withConfig(config.copy(collation = Some(c)))
  def verbose(b: Boolean): MapReduceQuery[A]         = withConfig(config.copy(verbose = Some(b)))
  def toCollection: MapReduceQuery[A]                = withConfig(config.copy(toCollection = true))
  def limit(i: Int): MapReduceQuery[A]               = withConfig(config.copy(limit = Some(i)))
  def maxTime(duration: Duration): MapReduceQuery[A] = withConfig(config.copy(maxTime = Some(duration)))

  override def take(n: Int) = limit(n)

  override def iterator = eval(config)

  override protected def onIterator[B](f: ResourceIterator[A] ⇒ ResourceIterator[B]) =
    new MapReduceQuery[B](config, eval andThen f)
}

object MapReduceQuery {
  final case class Config(action: Option[Action],
                          batchSize: Option[Int],
                          collectionName: Option[String],
                          databaseName: Option[String],
                          filter: Option[BsonDocument],
                          finalizeFunction: Option[String],
                          jsMode: Option[Boolean],
                          nonAtomic: Option[Boolean],
                          scope: Option[BsonDocument],
                          sharded: Option[Boolean],
                          sort: Option[BsonDocument],
                          collation: Option[Collation],
                          verbose: Option[Boolean],
                          toCollection: Boolean,
                          limit: Option[Int],
                          maxTime: Option[Duration])

  object Config {
    val empty: Config =
      Config(None, None, None, None, None, None, None, None, None, None, None, None, None, false, None, None)
  }

  private[mongodb] def from[R: BsonDocumentDecoder](
    f: ⇒ MapReduceIterable[BsonDocument]
  ): MapReduceQuery[MongoResult[R]] =
    new MapReduceQuery[MongoResult[R]](
      Config.empty,
      conf ⇒ {
        val iterable = f

        conf.action.foreach(a ⇒ iterable.action(a.toLegacy))
        conf.batchSize.foreach(iterable.batchSize)
        conf.collectionName.foreach(iterable.collectionName)
        conf.databaseName.foreach(iterable.databaseName)
        conf.filter.foreach(iterable.filter)
        conf.finalizeFunction.foreach(iterable.finalizeFunction)
        conf.jsMode.foreach(iterable.jsMode)
        conf.nonAtomic.foreach(iterable.nonAtomic)
        conf.scope.foreach(iterable.scope)
        conf.sharded.foreach(iterable.sharded)
        conf.sort.foreach(iterable.sort)
        conf.collation.foreach(c ⇒ iterable.collation(c.legacy))
        conf.verbose.foreach(iterable.verbose)
        if(conf.toCollection) iterable.toCollection()
        conf.limit.foreach(iterable.limit)
        conf.maxTime.foreach(d ⇒ iterable.maxTime(d.length, d.unit))

        MongoIterator(iterable)
      }
    )

  sealed abstract class Action extends Product with Serializable {
    def label: String
    private[mongodb] def toLegacy: MapReduceAction = this match {
      case Action.Replace ⇒ MapReduceAction.REPLACE
      case Action.Merge   ⇒ MapReduceAction.MERGE
      case Action.Reduce  ⇒ MapReduceAction.REDUCE
    }
  }

  object Action {
    case object Replace extends Action {
      override val label = "replace"
    }

    case object Merge extends Action {
      override val label = "merge"
    }

    case object Reduce extends Action {
      override val label = "reduce"
    }
  }
}
