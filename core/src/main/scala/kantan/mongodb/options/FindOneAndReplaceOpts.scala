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

import com.mongodb.client.model.{FindOneAndReplaceOptions, ReturnDocument}
import kantan.mongodb.{BsonDocument, BsonDocumentEncoder}
import scala.concurrent.duration.Duration

final case class FindOneAndReplaceOpts(collation: Option[Collation],
                                       maxTime: Option[Duration],
                                       projection: Option[BsonDocument],
                                       updated: Boolean,
                                       sort: Option[BsonDocument],
                                       upsert: Option[Boolean]) {
  def collation(c: Collation): FindOneAndReplaceOpts     = copy(collation = Some(c))
  def maxTime(duration: Duration): FindOneAndReplaceOpts = copy(maxTime = Some(duration))
  def projection[P: BsonDocumentEncoder](p: P): FindOneAndReplaceOpts =
    copy(projection = Some(BsonDocumentEncoder[P].encode(p)))
  def updated(b: Boolean): FindOneAndReplaceOpts = copy(updated = b)
  def sort[S: BsonDocumentEncoder](s: S): FindOneAndReplaceOpts =
    copy(sort = Some(BsonDocumentEncoder[S].encode(s)))
  def upsert(b: Boolean): FindOneAndReplaceOpts = copy(upsert = Some(b))

  private[mongodb] lazy val legacy: FindOneAndReplaceOptions = {
    val opts = new FindOneAndReplaceOptions()
      .returnDocument(if(updated) ReturnDocument.AFTER else ReturnDocument.BEFORE)

    upsert.foreach(opts.upsert)
    collation.foreach(c ⇒ opts.collation(c.legacy))
    maxTime.foreach(m ⇒ opts.maxTime(m.length, m.unit))
    projection.foreach(opts.projection)
    sort.foreach(opts.sort)

    opts
  }
}

object FindOneAndReplaceOpts {
  val default: FindOneAndReplaceOpts = FindOneAndReplaceOpts(None, None, None, false, None, None)
}
