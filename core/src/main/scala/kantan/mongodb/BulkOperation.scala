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

import com.mongodb.client.model._

sealed abstract class BulkOperation extends Product with Serializable {
  private[mongodb] def toModel: WriteModel[BsonDocument]
}

object BulkOperation {
  final case class DeleteMany[F: BsonDocumentEncoder](filter: F, options: DeleteOptions) extends BulkOperation {
    override private[mongodb] def toModel =
      new DeleteManyModel[BsonDocument](BsonDocumentEncoder[F].encode(filter), options)
  }

  final case class DeleteOne[F: BsonDocumentEncoder](filter: F, options: DeleteOptions) extends BulkOperation {
    override private[mongodb] def toModel =
      new DeleteOneModel[BsonDocument](BsonDocumentEncoder[F].encode(filter), options)
  }

  final case class InsertOne[D: BsonDocumentEncoder](document: D) extends BulkOperation {
    override private[mongodb] def toModel =
      new InsertOneModel[BsonDocument](BsonDocumentEncoder[D].encode(document))
  }

  final case class ReplaceOne[F: BsonDocumentEncoder, D: BsonDocumentEncoder](filter: F,
                                                                              document: D,
                                                                              options: UpdateOptions)
      extends BulkOperation {
    override private[mongodb] def toModel =
      new ReplaceOneModel[BsonDocument](
        BsonDocumentEncoder[F].encode(filter),
        BsonDocumentEncoder[D].encode(document),
        options
      )
  }

  final case class UpdateMany[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F,
                                                                              update: U,
                                                                              options: UpdateOptions)
      extends BulkOperation {
    override private[mongodb] def toModel =
      new UpdateManyModel[BsonDocument](
        BsonDocumentEncoder[F].encode(filter),
        BsonDocumentEncoder[U].encode(update),
        options
      )
  }

  final case class UpdateOne[F: BsonDocumentEncoder, U: BsonDocumentEncoder](filter: F,
                                                                             update: U,
                                                                             options: UpdateOptions)
      extends BulkOperation {
    override private[mongodb] def toModel =
      new UpdateOneModel[BsonDocument](
        BsonDocumentEncoder[F].encode(filter),
        BsonDocumentEncoder[U].encode(update),
        options
      )
  }
}
