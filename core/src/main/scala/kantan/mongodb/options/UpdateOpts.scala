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

import com.mongodb.client.model.UpdateOptions

final case class UpdateOpts(collation: Option[Collation], upsert: Option[Boolean]) {
  def upsert(u: Boolean): UpdateOpts = copy(upsert = Some(u))
  def collation(c: Collation): UpdateOpts = copy(collation = Some(c))

  private[mongodb] lazy val legacy: UpdateOptions = {
    val opts = new UpdateOptions()

    upsert.foreach(opts.upsert)
    collation.foreach(c ⇒ opts.collation(c.legacy))

    opts
  }
}

object UpdateOpts {
  val default: UpdateOpts = UpdateOpts(None, None)
}
