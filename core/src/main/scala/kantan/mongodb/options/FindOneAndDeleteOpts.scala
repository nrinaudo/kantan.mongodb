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

package kantan.mongodb.options

import com.mongodb.client.model.FindOneAndDeleteOptions
import kantan.mongodb.{BsonDocument, BsonDocumentEncoder}
import scala.concurrent.duration.Duration

final case class FindOneAndDeleteOpts(collation: Option[Collation],
                                      maxTime: Option[Duration],
                                      projection: Option[BsonDocument],
                                      sort: Option[BsonDocument]) {
  def collation(c: Collation): FindOneAndDeleteOpts     = copy(collation = Some(c))
  def maxTime(duration: Duration): FindOneAndDeleteOpts = copy(maxTime = Some(duration))
  def projection[P: BsonDocumentEncoder](p: P): FindOneAndDeleteOpts =
    copy(projection = Some(BsonDocumentEncoder[P].encode(p)))
  def sort[S: BsonDocumentEncoder](s: S): FindOneAndDeleteOpts = copy(sort = Some(BsonDocumentEncoder[S].encode(s)))

  private[mongodb] lazy val legacy: FindOneAndDeleteOptions = {
    val opts = new FindOneAndDeleteOptions()

    collation.foreach(c ⇒ opts.collation(c.legacy))
    maxTime.foreach(m ⇒ opts.maxTime(m.length, m.unit))
    projection.foreach(opts.projection)
    sort.foreach(opts.sort)

    opts
  }
}

object FindOneAndDeleteOpts {
  val default: FindOneAndDeleteOpts = FindOneAndDeleteOpts(None, None, None, None)
}
