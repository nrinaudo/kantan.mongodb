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
package options

import com.mongodb.client.model.{CreateCollectionOptions, IndexOptionDefaults}

final case class CreateCollectionOpts(autoIndex: Boolean,
                                      collation: Collation,
                                      indexOptions: Option[BsonDocument],
                                      cap: Option[CreateCollectionOpts.Cap],
                                      storageEngine: Option[BsonDocument],
                                      validation: ValidationOpts) {
  def autoIndex(b: Boolean): CreateCollectionOpts   = copy(autoIndex = b)
  def collation(c: Collation): CreateCollectionOpts = copy(collation = c)
  def indexOptions[I: BsonDocumentEncoder](i: I): CreateCollectionOpts =
    copy(indexOptions = Some(BsonDocumentEncoder[I].encode(i)))
  def cap(c: CreateCollectionOpts.Cap): CreateCollectionOpts = copy(cap = Some(c))
  def storageEngine[E: BsonDocumentEncoder](e: E): CreateCollectionOpts =
    copy(storageEngine = Some(BsonDocumentEncoder[E].encode(e)))
  def validation(v: ValidationOpts): CreateCollectionOpts = copy(validation = v)

  private[mongodb] lazy val legacy: CreateCollectionOptions = {
    val opts = new CreateCollectionOptions()
      .collation(collation.legacy)
      .validationOptions(validation.legacy)
      .autoIndex(autoIndex)

    cap.foreach { c ⇒
      opts.capped(true)
      opts.sizeInBytes(c.size)
      c.docs.foreach(opts.maxDocuments)
    }
    indexOptions.foreach(i ⇒ opts.indexOptionDefaults(new IndexOptionDefaults().storageEngine(i)))

    opts
  }
}

object CreateCollectionOpts {
  final case class Cap(size: Long, docs: Option[Long])

  val default: CreateCollectionOpts =
    CreateCollectionOpts(true, Collation.default, None, None, None, ValidationOpts.default)
}
