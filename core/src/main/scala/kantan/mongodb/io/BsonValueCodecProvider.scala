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

import kantan.mongodb._
import org.bson.BsonType
import org.bson.codecs.Codec
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}

object BsonValueCodecProvider extends CodecProvider {
  private val (bsonTypes, bsonClasses) = {
    val typeBuilder  = Map.newBuilder[BsonType, Class[_]]
    val classBuilder = Map.newBuilder[Class[_], CodecRegistry ⇒ Codec[_]]

    def add(c: Class[_], bsonType: BsonType, codec: CodecRegistry ⇒ Codec[_]): Unit = {
      typeBuilder  += bsonType → c
      classBuilder += c        → codec
      ()
    }

    add(classOf[BsonArray], BsonType.ARRAY, r ⇒ new ArrayCodec(r))
    add(classOf[BsonBinaryData], BsonType.BINARY, _ ⇒ BinaryDataCodec)
    add(classOf[BsonBoolean], BsonType.BOOLEAN, _ ⇒ BooleanCodec)
    add(classOf[BsonDateTime], BsonType.DATE_TIME, _ ⇒ DateTimeCodec)
    add(classOf[BsonDbPointer], BsonType.DB_POINTER, _ ⇒ DbPointerCodec)
    add(classOf[BsonDocument], BsonType.DOCUMENT, r ⇒ new DocumentCodec(r))
    add(classOf[BsonDouble], BsonType.DOUBLE, _ ⇒ DoubleCodec)
    add(classOf[BsonDecimal128], BsonType.DECIMAL128, _ ⇒ Decimal128Codec)
    add(classOf[BsonInt], BsonType.INT32, _ ⇒ IntCodec)
    add(classOf[BsonJavaScript], BsonType.JAVASCRIPT, _ ⇒ JavaScriptCodec)
    add(classOf[BsonJavaScriptWithScope], BsonType.JAVASCRIPT_WITH_SCOPE, r ⇒ new JavaScriptWithScopeCodec(r))
    add(classOf[BsonLong], BsonType.INT64, _ ⇒ LongCodec)
    add(BsonMaxKey.getClass, BsonType.MAX_KEY, _ ⇒ MaxKeyCodec)
    add(BsonMinKey.getClass, BsonType.MIN_KEY, _ ⇒ MinKeyCodec)
    add(BsonNull.getClass, BsonType.NULL, _ ⇒ NullCodec)
    add(classOf[BsonObjectId], BsonType.OBJECT_ID, _ ⇒ ObjectIdCodec)
    add(classOf[BsonRegularExpression], BsonType.REGULAR_EXPRESSION, _ ⇒ RegularExpressionCodec)
    add(classOf[BsonString], BsonType.STRING, _ ⇒ StringCodec)
    add(classOf[BsonSymbol], BsonType.SYMBOL, _ ⇒ SymbolCodec)
    add(classOf[BsonTimestamp], BsonType.TIMESTAMP, _ ⇒ TimestampCodec)
    add(BsonUndefined.getClass, BsonType.UNDEFINED, _ ⇒ UndefinedCodec)

    classBuilder += classOf[BsonValue]             → (r ⇒ new ValueCodec(r))
    classBuilder += classOf[BsonBinary]            → (_ ⇒ BinaryCodec)
    classBuilder += classOf[BsonFunction]          → (_ ⇒ FunctionCodec)
    classBuilder += classOf[BsonUserDefinedBinary] → (_ ⇒ UserDefinedBinaryCodec)
    classBuilder += classOf[BsonMd5]               → (_ ⇒ Md5Codec)
    classBuilder += classOf[BsonUuid]              → (_ ⇒ UuidCodec)

    (typeBuilder.result(), classBuilder.result())
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "org.wartremover.warts.Null"))
  override def get[T](c: Class[T], registry: CodecRegistry): Codec[T] =
    bsonClasses.get(c).map(f ⇒ f(registry)).getOrElse(null).asInstanceOf[Codec[T]]

  // This fails when btype is not handled. Not much else I can do, I'm afraid...
  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private[io] def codecFor(registry: CodecRegistry, btype: BsonType): Codec[BsonValue] =
    registry.get(bsonTypes(btype)).asInstanceOf[Codec[BsonValue]]

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private[io] def codecFor[T](registry: CodecRegistry, cls: Class[T]): Codec[BsonValue] =
    registry.get(cls).asInstanceOf[Codec[BsonValue]]
}
