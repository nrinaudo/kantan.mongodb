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

final case class UpdateOpts(bypassDocumentValidation: Boolean, collation: Collation, upsert: Boolean) {
  def upsert(u: Boolean): UpdateOpts = copy(upsert = u)
  def collation(c: Collation): UpdateOpts = copy(collation = c)
  def bypassDocumentValidation(b: Boolean): UpdateOpts = copy(bypassDocumentValidation = b)

  private[mongodb] lazy val legacy: UpdateOptions =
    new UpdateOptions().upsert(upsert)
      .bypassDocumentValidation(bypassDocumentValidation)
      .collation(collation.legacy)
}

object UpdateOpts {
  val default: UpdateOpts = UpdateOpts(false, Collation.default, false)
}
