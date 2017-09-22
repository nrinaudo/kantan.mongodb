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

import kantan.mongodb.BsonBoolean
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}

object BooleanCodec extends Codec[BsonBoolean] {
  override def decode(reader: BsonReader, d: DecoderContext)                     = BsonBoolean(reader.readBoolean())
  override def encode(writer: BsonWriter, value: BsonBoolean, e: EncoderContext) = writer.writeBoolean(value.value)
  override def getEncoderClass                                                   = classOf[BsonBoolean]
}
