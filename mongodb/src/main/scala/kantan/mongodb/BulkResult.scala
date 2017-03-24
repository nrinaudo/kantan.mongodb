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

import com.mongodb.bulk.BulkWriteResult
import kantan.bson.{BsonValue, BsonValueDecoder, DecodeResult}
import scala.collection.JavaConverters._

final case class BulkResult(inserted: Int, matched: Int, deleted: Int, modified: Option[Int],
                            acknowledged: Boolean, upserts: Seq[BulkResult.Upsert])

object BulkResult {
  private[mongodb] def apply(res: BulkWriteResult): BulkResult =
    BulkResult(res.getInsertedCount, res.getMatchedCount, res.getDeletedCount, Option(res.getModifiedCount),
      res.wasAcknowledged(), res.getUpserts.asScala.map(u ⇒ Upsert(u.getIndex, BsonValue.fromLegacy(u.getId))))

  final case class Upsert(index: Int, rawId: BsonValue) {
    def id[A: BsonValueDecoder]: DecodeResult[A] = BsonValueDecoder[A].decode(rawId)
  }
}
