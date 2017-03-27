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

import com.mongodb.client.model.CountOptions
import kantan.mongodb.{BsonDocument, BsonDocumentEncoder}
import scala.concurrent.duration.Duration

final case class CountOpts(collation: Collation, hint: Option[Either[BsonDocument, String]], limit: Option[Int],
                           maxTime: Option[Duration], skip: Int) {
  def collation(c: Collation): CountOpts = copy(collation = c)
  def hint[H: BsonDocumentEncoder](h: H): CountOpts = copy(hint = Some(Left(BsonDocumentEncoder[H].encode(h))))
  def hint(string: String): CountOpts = copy(hint = Some(Right(string)))
  def clearHint: CountOpts = copy(hint = None)
  def limit(i: Int): CountOpts = copy(limit = Some(i))
  def clearLimit: CountOpts = copy(limit = None)
  def maxTime(duration: Duration): CountOpts = copy(maxTime = Some(duration))
  def clearMaxTime: CountOpts = copy(maxTime = None)
  def skip(i: Int): CountOpts = copy(skip = i)

  private[mongodb] lazy val legacy: CountOptions = {
    val opts = new CountOptions().collation(collation.legacy)
      .skip(skip)

    hint.foreach {
      case Left(doc)  ⇒ opts.hint(doc)
      case Right(str) ⇒ opts.hintString(str)
    }
    limit.foreach(opts.limit)
    maxTime.foreach(m ⇒ opts.maxTime(m.length, m.unit))

    opts
  }
}

object CountOpts {
  val default: CountOpts = CountOpts(Collation.default, None, None, None, 0)
}
