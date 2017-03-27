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
import kantan.codecs.resource.ResourceIterable
import scala.concurrent.duration.{Duration, TimeUnit}

abstract class MapReduceQuery[A] extends ResourceIterable[A] {
  def action(a: MapReduceQuery.Action): MapReduceQuery[A]
  def batchSize(i: Int): MapReduceQuery[A]
  def bypassDocumentValidation(b: Boolean): MapReduceQuery[A]
  def collectionName(n: String): MapReduceQuery[A]
  def databaseName(n: String): MapReduceQuery[A]
  def filter[F: BsonDocumentEncoder](filter: F): MapReduceQuery[A]
  def finalizeFunction(s: String): MapReduceQuery[A]
  def jsMode(m: Boolean): MapReduceQuery[A]
  def nonAtomic(b: Boolean): MapReduceQuery[A]
  def scope[S: BsonDocumentEncoder](scope: S): MapReduceQuery[A]
  def sharded(s: Boolean): MapReduceQuery[A]
  def sort[S: BsonDocumentEncoder](sort: S): MapReduceQuery[A]
  def collation(c: Collation): MapReduceQuery[A]
  def verbose(b: Boolean): MapReduceQuery[A]
  def toCollection: MapReduceQuery[A]
  def limit(i: Int): MapReduceQuery[A]
  def maxTime(l: Long, u: TimeUnit): MapReduceQuery[A]
  override final def toString = s"${getClass.getName}@${Integer.toHexString(hashCode())}"
}

object MapReduceQuery {
  private[mongodb] def from[R: BsonDocumentDecoder](f: ⇒ MapReduceIterable[BsonDocument])
  : MapReduceQuery[MongoResult[R]] = MapReduceQueryImpl(None, None, None, None, None, None, None, None, None, None,
    None, None, None, None, false, None, None, () ⇒ f)
  private final case class MapReduceQueryImpl[A: BsonDocumentDecoder](
                                                                       action: Option[Action],
                                                                       batch: Option[Int],
                                                                       bypassValidation: Option[Boolean],
                                                                       collection: Option[String],
                                                                       database: Option[String],
                                                                       filter: Option[BsonDocument],
                                                                       finalizer: Option[String],
                                                                       js: Option[Boolean],
                                                                       nonAtom: Option[Boolean],
                                                                       scp: Option[BsonDocument],
                                                                       shrd: Option[Boolean],
                                                                       srt: Option[BsonDocument],
                                                                       col: Option[Collation],
                                                                       verb: Option[Boolean],
                                                                       toCol: Boolean,
                                                                       max: Option[Int],
                                                                       time: Option[Duration],
                                                                       eval: () ⇒ MapReduceIterable[BsonDocument]
                                                                     ) extends MapReduceQuery[MongoResult[A]] {
    override def action(a: Action) = copy(action = Some(a))
    override def batchSize(i: Int) = copy(batch = Some(i))
    override def bypassDocumentValidation(b: Boolean) = copy(bypassValidation = Some(b))
    override def collectionName(n: String) = copy(collection = Some(n))
    override def databaseName(n: String) = copy(database = Some(n))
    override def filter[F: BsonDocumentEncoder](f: F) = copy(filter = Some(BsonDocumentEncoder[F].encode(f)))
    override def finalizeFunction(s: String) = copy(finalizer = Some(s))
    override def jsMode(m: Boolean) = copy(js = Some(m))
    override def nonAtomic(b: Boolean) = copy(nonAtom = Some(b))
    override def scope[S: BsonDocumentEncoder](s: S) = copy(scp = Some(BsonDocumentEncoder[S].encode(s)))
    override def sharded(s: Boolean) = copy(shrd = Some(s))
    override def sort[S: BsonDocumentEncoder](s: S) = copy(srt = Some(BsonDocumentEncoder[S].encode(s)))
    override def collation(c: Collation) = copy(col = Some(c))
    override def verbose(b: Boolean) = copy(verb = Some(b))
    override def toCollection = copy(toCol = true)
    override def limit(i: Int) = copy(max = Some(i))
    override def maxTime(l: Long, u: TimeUnit) = copy(time = Some(Duration(l, u)))
    override def iterator = {
      val iterable = eval()

      action.foreach(a ⇒ iterable.action(a.toLegacy))
      batch.foreach(iterable.batchSize)
      bypassValidation.foreach(b ⇒ iterable.bypassDocumentValidation(b))
      collection.foreach(iterable.collectionName)
      database.foreach(iterable.databaseName)
      filter.foreach(iterable.filter)
      finalizer.foreach(iterable.finalizeFunction)
      js.foreach(iterable.jsMode)
      nonAtom.foreach(iterable.nonAtomic)
      scp.foreach(iterable.scope)
      shrd.foreach(iterable.sharded)
      srt.foreach(iterable.sort)
      col.foreach(iterable.collation)
      verb.foreach(iterable.verbose)
      if(toCol) iterable.toCollection()
      max.foreach(iterable.limit)
      time.foreach(d ⇒ iterable.maxTime(d.length, d.unit))

      MongoIterator(iterable)
    }
  }

  sealed abstract class Action extends Product with Serializable {
    def label: String
    private[mongodb] def toLegacy: MapReduceAction = this match {
      case Action.Replace ⇒ MapReduceAction.REPLACE
      case Action.Merge   ⇒ MapReduceAction.MERGE
      case Action.Reduce ⇒ MapReduceAction.REDUCE
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
