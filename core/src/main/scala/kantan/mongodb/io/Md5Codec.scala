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

import kantan.mongodb.BsonMd5
import org.bson.{BsonBinarySubType, BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}

object Md5Codec extends Codec[BsonMd5] {
  override def decode(reader: BsonReader, d: DecoderContext) = BsonMd5.hex(reader.readBinaryData().getData)

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def hexStringToByteArray(s: String): Array[Byte] = {
    val len  = s.length()
    val data = new Array[Byte](len / 2)

    data.indices.foreach { i ⇒
      data(i) = ((Character.digit(s.charAt(i * 2), 16) << 4)
        + Character.digit(s.charAt(i * 2 + 1), 16)).asInstanceOf[Byte]
    }

    data
  }

  override def encode(writer: BsonWriter, value: BsonMd5, e: EncoderContext) =
    writer.writeBinaryData(new org.bson.BsonBinary(BsonBinarySubType.MD5, hexStringToByteArray(value.value)))

  override def getEncoderClass = classOf[BsonMd5]
}
