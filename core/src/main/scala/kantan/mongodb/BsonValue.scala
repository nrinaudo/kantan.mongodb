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

import java.util.UUID
import java.util.regex.Pattern
import kantan.mongodb.io._
import org.bson.{BsonBinarySubType, RawBsonDocument}
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson
import org.bson.types.{Decimal128, ObjectId}
import scala.collection.JavaConverters._

/** Represents all possible values that can be found in a BSON document. */
sealed abstract class BsonValue extends Product with Serializable

object BsonValue {
  /** Turns a legacy `BsonValue` into our AST.
    *
    * This is meant to cover yet another of these mongo warts: even though we can register encoders / decoders,
    * sometimes, the API will force its own representations.
    */
  def fromLegacy(value: org.bson.BsonValue): BsonValue =
    if(value.isArray) BsonArray(value.asArray().getValues().asScala.map(fromLegacy))
    else if(value.isBinary) BsonBinaryData.fromLegacy(value.asBinary())
    else if(value.isBoolean) BsonBoolean(value.asBoolean().getValue)
    else if(value.isDateTime) BsonDateTime(value.asDateTime().getValue)
    else if(value.isDBPointer) BsonDbPointer.fromLegacy(value.asDBPointer())
    else if(value.isDecimal128) BsonDecimal128(value.asDecimal128().getValue)
    else if(value.isDocument) BsonDocument.fromLegacy(value.asDocument())
    else if(value.isDouble) BsonDouble(value.asDouble().getValue)
    else if(value.isInt32) BsonInt(value.asInt32().getValue)
    else if(value.isInt64) BsonLong(value.asInt64().getValue)
    else if(value.isJavaScript) BsonJavaScript(value.asJavaScript().getCode)
    else if(value.isJavaScriptWithScope) BsonJavaScriptWithScope.fromLegacy(value.asJavaScriptWithScope())
    else if(value.isNull) BsonNull
    else if(value.isObjectId) BsonObjectId(value.asObjectId().getValue)
    else if(value.isRegularExpression) BsonRegularExpression.fromLegacy(value.asRegularExpression())
    else if(value.isString) BsonString(value.asString().getValue)
    else if(value.isSymbol) BsonSymbol(value.asSymbol().getSymbol)
    else if(value.isTimestamp) BsonTimestamp.fromLegacy(value.asTimestamp())
    else sys.error(s"Unexpected legacy BSON value: $value")
}


// - Binary data -------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
sealed abstract class BsonBinaryData extends BsonValue

object BsonBinaryData {
  import BsonBinarySubType._

  def isUuid(subtype: Byte): Boolean =
    subtype == UUID_LEGACY.getValue || subtype  == UUID_STANDARD.getValue

  def isBinary(subtype: Byte): Boolean = subtype == BINARY.getValue || subtype == OLD_BINARY.getValue

  def fromLegacy(b: org.bson.BsonBinary): BsonBinaryData =
    if(isBinary(b.getType))                     BsonBinary(b.getData)
    else if(isUuid(b.getType))                  BsonUuid(UuidHelper.decode(b.getData, b.getType))
    else if(b.getType == MD5.getValue)          BsonMd5.hex(b.getData)
    else if(b.getType == USER_DEFINED.getValue) BsonFunction(b.getData)
    else if(b.getType == FUNCTION.getValue)     BsonFunction(b.getData)
    else                                        sys.error(s"Unsupported binary subtype: ${b.getType}")
}


final case class BsonUuid(value: UUID) extends BsonBinaryData
final case class BsonMd5(value: String) extends BsonBinaryData
final case class BsonUserDefinedBinary(value: IndexedSeq[Byte]) extends BsonBinaryData
final case class BsonFunction(value: IndexedSeq[Byte]) extends BsonBinaryData

object BsonMd5 {
  def hex(data: Array[Byte]): BsonMd5 = BsonMd5(data.map("%02x".format(_)).mkString)
}

final case class BsonBinary(value: IndexedSeq[Byte]) extends BsonBinaryData



// - Nested types ------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
final case class BsonArray(value: Seq[BsonValue]) extends BsonValue
final case class BsonJavaScriptWithScope(value: String, scope: Map[String, BsonValue]) extends BsonValue

object BsonJavaScriptWithScope {
  def fromLegacy(j: org.bson.BsonJavaScriptWithScope): BsonJavaScriptWithScope =
    BsonJavaScriptWithScope(j.getCode, BsonDocument.valuesFromLegacy(j.getScope))
}

final case class BsonDocument(value: Map[String, BsonValue]) extends BsonValue with Bson {
  override def toBsonDocument[A](documentClass: Class[A], codecRegistry: CodecRegistry) =
    new RawBsonDocument(this, new DocumentCodec(codecRegistry))
}

object BsonDocument {
  val empty: BsonDocument = BsonDocument(Map.empty)

  private[mongodb] def valuesFromLegacy(d: org.bson.BsonDocument): Map[String, BsonValue] = {
    val builder = Map.newBuilder[String, BsonValue]

    d.keySet().asScala.foreach { k ⇒ builder += k → BsonValue.fromLegacy(d.get(k)) }

    builder.result()
  }
  def fromLegacy(d: org.bson.BsonDocument): BsonDocument = BsonDocument(valuesFromLegacy(d))
}



// - Singleton types ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
case object BsonMaxKey extends BsonValue
case object BsonMinKey extends BsonValue
case object BsonNull extends BsonValue
case object BsonUndefined extends BsonValue



// - Simple values -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
final case class BsonBoolean(value: Boolean) extends BsonValue
final case class BsonDateTime(value: Long) extends BsonValue
final case class BsonDecimal128(value: Decimal128) extends BsonValue
final case class BsonJavaScript(value: String) extends BsonValue
final case class BsonDouble(value: Double) extends BsonValue
final case class BsonInt(value: Int) extends BsonValue
final case class BsonLong(value: Long) extends BsonValue
final case class BsonObjectId(value: ObjectId) extends BsonValue
final case class BsonString(value: String) extends BsonValue
final case class BsonSymbol(value: String) extends BsonValue

final case class BsonTimestamp(seconds: Int, inc: Int) extends BsonValue

object BsonTimestamp {
  def fromLegacy(t: org.bson.BsonTimestamp): BsonTimestamp = BsonTimestamp(t.getTime, t.getInc)
  def toLegacy(t: BsonTimestamp): org.bson.BsonTimestamp = new org.bson.BsonTimestamp(t.seconds, t.inc)
}

final case class BsonRegularExpression(value: Pattern) extends BsonValue {
  override def equals(obj: Any): Boolean = obj match {
    case BsonRegularExpression(p) ⇒ p.pattern() == value.pattern() && p.flags() == value.flags()
    case _ ⇒ false
  }

  override def hashCode(): Int = 31 * value.pattern().hashCode + value.flags()
}

object BsonRegularExpression {
  private val patternFlags: List[(Int, Char)] = List(
    Pattern.CANON_EQ         → 'c',
    Pattern.UNIX_LINES       → 'd',
    256                      → 'g',
    Pattern.CASE_INSENSITIVE → 'i',
    Pattern.MULTILINE        → 'm',
    Pattern.DOTALL           → 's',
    Pattern.LITERAL          → 't',
    Pattern.UNICODE_CASE     → 'u',
    Pattern.COMMENTS         → 'x'
  )

  private def optionsAsInt(flags: String): Int = flags.foldLeft(0) { case (acc, flag) ⇒
    patternFlags.find(_._2.toLower == flag).map(_._1).fold(acc)(acc | _)
  }

  private def optionsAsString(flags: Int): String =
    patternFlags.foldLeft(new StringBuilder()) { case (acc, (i, c)) ⇒
      if((flags & i) > 0) acc.append(c)
      else                acc
    }.result()

  def fromLegacy(r: org.bson.BsonRegularExpression): BsonRegularExpression =
    BsonRegularExpression(Pattern.compile(r.getPattern, optionsAsInt(r.getOptions)))
  def toLegacy(r: BsonRegularExpression): org.bson.BsonRegularExpression =
    new org.bson.BsonRegularExpression(r.value.pattern(), optionsAsString(r.value.flags()))
}

final case class BsonDbPointer(namespace: String, id: ObjectId) extends BsonValue

object BsonDbPointer {
  def fromLegacy(p: org.bson.BsonDbPointer): BsonDbPointer = BsonDbPointer(p.getNamespace, p.getId)
  def toLegacy(p: BsonDbPointer): org.bson.BsonDbPointer = new org.bson.BsonDbPointer(p.namespace, p.id)
}
