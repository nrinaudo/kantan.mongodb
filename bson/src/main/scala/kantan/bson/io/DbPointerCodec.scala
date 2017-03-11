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

package kantan.bson.io

import kantan.bson.BsonDbPointer
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}

object DbPointerCodec extends Codec[BsonDbPointer] {
  override def decode(reader: BsonReader, d: DecoderContext) = {
    val p = reader.readDBPointer
    BsonDbPointer(p.getNamespace, p.getId)
  }
  override def encode(writer: BsonWriter, value: BsonDbPointer, e: EncoderContext) =
    writer.writeDBPointer(new org.bson.BsonDbPointer(value.namespace, value.id))

  override def getEncoderClass = classOf[BsonDbPointer]
}
