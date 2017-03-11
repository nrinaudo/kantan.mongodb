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

import kantan.bson.{BsonBinary, BsonBinaryData, BsonFunction, BsonUserDefinedBinary}
import org.bson.{BsonBinarySubType, BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}

trait RawBinaryCodec[T <: BsonBinaryData] extends Codec[T] {
  def wrap(data: IndexedSeq[Byte]): T
  def unwrap(t: T): Array[Byte]
  def subtype: BsonBinarySubType

  override def decode(reader: BsonReader, decoderContext: DecoderContext) = {
    val b = reader.readBinaryData()
    wrap(b.getData())
  }

  override def encode(writer: BsonWriter, value: T, e: EncoderContext) =
    writer.writeBinaryData(new org.bson.BsonBinary(subtype, unwrap(value)))
}

object BinaryCodec extends RawBinaryCodec[BsonBinary] {
  override val subtype = BsonBinarySubType.BINARY
  override def wrap(data: IndexedSeq[Byte]) = BsonBinary(data)
  override def unwrap(t: BsonBinary) = t.value.toArray
  override def getEncoderClass = classOf[BsonBinary]
}

object UserDefinedBinaryCodec extends RawBinaryCodec[BsonUserDefinedBinary] {
  override val subtype = BsonBinarySubType.USER_DEFINED
  override def wrap(data: IndexedSeq[Byte]) = BsonUserDefinedBinary(data)
  override def unwrap(t: BsonUserDefinedBinary) = t.value.toArray
  override def getEncoderClass = classOf[BsonUserDefinedBinary]
}

object FunctionCodec extends RawBinaryCodec[BsonFunction] {
  override val subtype = BsonBinarySubType.FUNCTION
  override def wrap(data: IndexedSeq[Byte]) = BsonFunction(data)
  override def unwrap(t: BsonFunction) = t.value.toArray
  override def getEncoderClass = classOf[BsonFunction]
}