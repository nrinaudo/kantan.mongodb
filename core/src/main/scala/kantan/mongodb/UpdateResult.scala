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

sealed abstract class UpdateResult extends Product with Serializable

object UpdateResult {
  private[mongodb] def apply(result: com.mongodb.client.result.UpdateResult): UpdateResult =
    if(result.getUpsertedId == null)
      Update(result.getMatchedCount, if(result.isModifiedCountAvailable) Some(result.getModifiedCount) else None)
    else Upsert(BsonValue.fromLegacy(result.getUpsertedId))

  final case class Upsert(rawId: BsonValue) extends UpdateResult {
    def id[A: BsonValueDecoder]: DecodeResult[A] = BsonValueDecoder[A].decode(rawId)
  }

  final case class Update(matched: Long, modified: Option[Long]) extends UpdateResult
}
