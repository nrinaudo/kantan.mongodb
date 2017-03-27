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

import com.mongodb.client.model.{FindOneAndUpdateOptions, ReturnDocument}
import kantan.mongodb.{BsonDocument, BsonDocumentEncoder}
import scala.concurrent.duration.Duration

final case class FindOneAndUpdateOpts(bypassDocumentValidation: Boolean, collation: Collation,
                                      maxTime: Option[Duration], projection: Option[BsonDocument],
                                      sort: Option[BsonDocument], updated: Boolean, upsert: Boolean) {
  def bypassDocumentValidation(b: Boolean): FindOneAndUpdateOpts = copy(bypassDocumentValidation = b)
  def collation(c: Collation): FindOneAndUpdateOpts = copy(collation = c)
  def maxTime(duration: Duration): FindOneAndUpdateOpts = copy(maxTime = Some(duration))
  def clearMaxTime: FindOneAndUpdateOpts = copy(maxTime = None)
  def projection[P: BsonDocumentEncoder](p: P): FindOneAndUpdateOpts =
    copy(projection = Some(BsonDocumentEncoder[P].encode(p)))
  def clearProjection: FindOneAndUpdateOpts = copy(projection = None)
  def sort[S: BsonDocumentEncoder](s: S): FindOneAndUpdateOpts = copy(sort = Some(BsonDocumentEncoder[S].encode(s)))
  def clearSort: FindOneAndUpdateOpts = copy(sort = None)
  def updated(b: Boolean): FindOneAndUpdateOpts = copy(updated = b)
  def upsert(b: Boolean): FindOneAndUpdateOpts = copy(upsert = b)

  private[mongodb] lazy val legacy: FindOneAndUpdateOptions = {
    val opts = new FindOneAndUpdateOptions().bypassDocumentValidation(bypassDocumentValidation)
      .collation(collation.legacy)
      .returnDocument(if(updated) ReturnDocument.AFTER else ReturnDocument.BEFORE)
      .upsert(upsert)

    maxTime.foreach(m ⇒ opts.maxTime(m.length, m.unit))
    projection.foreach(p ⇒ opts.projection(p))
    sort.foreach(s ⇒ opts.sort(s))

    opts
  }
}

object FindOneAndUpdateOpts {
  val default: FindOneAndUpdateOpts = FindOneAndUpdateOpts(false, Collation.default, None, None, None, false, false)
}
