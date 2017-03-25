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

package kantan.mongodb.io

import kantan.mongodb.{BsonDocument, BsonValue}
import org.bson.{BsonReader, BsonType, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.CodecRegistry
import scala.annotation.tailrec
import scala.collection.mutable

class DocumentCodec(val registry: CodecRegistry) extends Codec[BsonDocument] {
  override def decode(reader: BsonReader, context: DecoderContext) = {
    @tailrec
    def loop(builder: mutable.Builder[(String, BsonValue), Map[String, BsonValue]]): Map[String, BsonValue] =
      if(reader.readBsonType == BsonType.END_OF_DOCUMENT) {
        reader.readEndDocument()
        builder.result()
      }
      else loop(builder += reader.readName → BsonValueCodecProvider.codecFor(registry, reader.getCurrentBsonType)
        .decode(reader, context))

    reader.readStartDocument()
    BsonDocument(loop(Map.newBuilder[String, BsonValue]))
  }

  override def encode(writer: BsonWriter, value: BsonDocument, context: EncoderContext) = {
    writer.writeStartDocument()

    value.value.foreach { case (name, v) ⇒
      writer.writeName(name)
      context.encodeWithChildContext(BsonValueCodecProvider.codecFor(registry, v.getClass), writer, v)
    }

    writer.writeEndDocument()
  }
  override def getEncoderClass = classOf[BsonDocument]
}
