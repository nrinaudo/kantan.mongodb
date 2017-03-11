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

import kantan.bson.{BsonArray, BsonValue}
import org.bson.{BsonReader, BsonType, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.CodecRegistry

class ArrayCodec(val registry: CodecRegistry) extends Codec[BsonArray] {
  override def decode(reader: BsonReader, context: DecoderContext) = {
    reader.readStartArray()

    val builder = Seq.newBuilder[BsonValue]
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      builder += BsonValueCodecProvider.codecFor(registry, reader.getCurrentBsonType).decode(reader, context)
    }

    reader.readEndArray()
    BsonArray(builder.result())
  }

  override def encode(writer: BsonWriter, value: BsonArray, context: EncoderContext) = {
    writer.writeStartArray()

    value.value.foreach { v ⇒
      context.encodeWithChildContext(BsonValueCodecProvider.codecFor(registry, v.getClass), writer, v)
    }

    writer.writeEndArray()
  }
  override def getEncoderClass = classOf[BsonArray]
}
