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

import com.mongodb.client.result.{DeleteResult ⇒ DResult}

/** Represents the result of a delete operation.
  *
  * Possible values are [[DeleteResult.Acknowledged]] and [[DeleteResult.Unacknowledged]].
  */
sealed abstract class DeleteResult(val acknowledged: Boolean) extends Product with Serializable

object DeleteResult {
  private[mongodb] def fromLegacy(legacy: DResult): DeleteResult =
    if(legacy.wasAcknowledged()) Acknowledged(legacy.getDeletedCount)
    else Unacknowledged

  /** Represents a delete operation that was acknowledged by the server.
    *
    * [[deleted]] is the number of documents that were deleted.
    */
  final case class Acknowledged(deleted: Long) extends DeleteResult(true)

  /** Represents a delete operation that was not acknowledged by the server. */
  case object Unacknowledged extends DeleteResult(false)
}
