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

import kantan.bson._
import org.bson.{BsonBinarySubType, BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import BsonBinarySubType._

object BinaryDataCodec extends Codec[BsonBinaryData] {
  private def decoderFor(subtype: Byte) =
    if(subtype == BINARY.getValue || subtype == OLD_BINARY.getValue)              BinaryCodec
    else if(subtype == UUID_LEGACY.getValue || subtype == UUID_STANDARD.getValue) UuidCodec
    else if(subtype == MD5.getValue)                                              Md5Codec
    else if(subtype == USER_DEFINED.getValue)                                     UserDefinedBinaryCodec
    else if(subtype == FUNCTION.getValue)                                         FunctionCodec
    else sys.error(s"Unsupported binary subtype: $subtype")

  override def decode(reader: BsonReader, decoderContext: DecoderContext) =
    decoderFor(reader.peekBinarySubType()).decode(reader, decoderContext)

  private def encoderFor[T](data: BsonBinaryData) = data match {
    case BsonBinary(_)            ⇒ BinaryCodec.asInstanceOf[Codec[BsonBinaryData]]
    case BsonMd5(_)               ⇒ Md5Codec.asInstanceOf[Codec[BsonBinaryData]]
    case BsonUuid(_)              ⇒ UuidCodec.asInstanceOf[Codec[BsonBinaryData]]
    case BsonFunction(_)          ⇒ FunctionCodec.asInstanceOf[Codec[BsonBinaryData]]
    case BsonUserDefinedBinary(_) ⇒ UserDefinedBinaryCodec.asInstanceOf[Codec[BsonBinaryData]]
  }

  override def encode(writer: BsonWriter, value: BsonBinaryData, e: EncoderContext) =
    encoderFor(value).encode(writer, value, e)

  override def getEncoderClass = classOf[BsonBinaryData]
}
