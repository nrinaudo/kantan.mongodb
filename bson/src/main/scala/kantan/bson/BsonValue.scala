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

package kantan.bson

import java.util.UUID
import java.util.regex.Pattern
import org.bson.types.{Decimal128, ObjectId}

/** Represents all possible values that can be found in a BSON document. */
sealed abstract class BsonValue extends Product with Serializable


// - Binary data -------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
sealed abstract class BsonBinaryData extends BsonValue
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
final case class BsonDocument(value: Map[String, BsonValue]) extends BsonValue
final case class BsonJavaScriptWithScope(value: String, scope: Map[String, BsonValue]) extends BsonValue



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
final case class BsonDbPointer(namespace: String, id: ObjectId) extends BsonValue
final case class BsonDecimal128(value: Decimal128) extends BsonValue
final case class BsonJavaScript(value: String) extends BsonValue
final case class BsonDouble(value: Double) extends BsonValue
final case class BsonInt(value: Int) extends BsonValue
final case class BsonLong(value: Long) extends BsonValue
final case class BsonObjectId(value: ObjectId) extends BsonValue
final case class BsonString(value: String) extends BsonValue
final case class BsonSymbol(value: String) extends BsonValue
final case class BsonTimestamp(seconds: Int, inc: Int) extends BsonValue

final case class BsonRegularExpression(value: Pattern) extends BsonValue {
  override def equals(obj: Any): Boolean = obj match {
    case BsonRegularExpression(p) ⇒ p.pattern() == value.pattern() && p.flags() == value.flags()
    case _ ⇒ false
  }

  override def hashCode(): Int = 31 * value.pattern().hashCode + value.flags()
}
