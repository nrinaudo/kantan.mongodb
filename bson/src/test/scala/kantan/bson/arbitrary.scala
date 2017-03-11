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

import org.scalacheck.{Arbitrary, Gen}
import Arbitrary.{arbitrary => arb}
import Gen._
import java.security.MessageDigest
import java.util.regex.Pattern
import org.bson.types.{Decimal128, ObjectId}

object arbitrary {
  // - BSON types ------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  val genObjectId: Gen[ObjectId] = for {
      ts ← posNum[Int]
      machine ← posNum[Int]
      process ← posNum[Short]
      counter ← posNum[Int]
    } yield new ObjectId(ts, machine, process, counter)

  val genDecimal128: Gen[Decimal128] = arb[Long].map(l ⇒ new Decimal128(l))

  val genRegexOptions: Gen[Int] = listOf(oneOf(Pattern.UNIX_LINES, 256, Pattern.CANON_EQ, Pattern.CASE_INSENSITIVE,
    Pattern.MULTILINE, Pattern.DOTALL, Pattern.LITERAL, Pattern.UNICODE_CASE, Pattern.COMMENTS))
    .map(_.toSet.foldLeft(0)(_ | _))



  // - Binary data -----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  val genBsonBinary: Gen[BsonBinary] = for {
    bytes ← buildableOf[IndexedSeq[Byte], Byte](arb[Byte])
  } yield BsonBinary(bytes)

  val genBsonFunction: Gen[BsonFunction] = for {
    bytes ← buildableOf[IndexedSeq[Byte], Byte](arb[Byte])
  } yield BsonFunction(bytes)

  val genBsonUserDefinedBinary: Gen[BsonUserDefinedBinary] = for {
      bytes ← buildableOf[IndexedSeq[Byte], Byte](arb[Byte])
    } yield BsonUserDefinedBinary(bytes)

  val genBsonUuid: Gen[BsonUuid] = uuid.map(BsonUuid.apply)
  val genBsonMd5: Gen[BsonMd5] = for {
    str ← arb[String]
  } yield BsonMd5.hex(MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8")))
  val genBsonBinaryData: Gen[BsonBinaryData] =
    oneOf(genBsonUuid, genBsonMd5, genBsonBinary, genBsonFunction, genBsonUserDefinedBinary)



  // - Terminal types --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  val genBsonObjectId: Gen[BsonObjectId] = genObjectId.map(BsonObjectId.apply)
  val genBsonDecimal128: Gen[BsonDecimal128] = genDecimal128.map(BsonDecimal128.apply)
  val genBsonBoolean: Gen[BsonBoolean] = arb[Boolean].map(BsonBoolean.apply)
  val genBsonDouble: Gen[BsonDouble] =  arb[Double].map(BsonDouble.apply)
  val genBsonInt: Gen[BsonInt] = arb[Int].map(BsonInt.apply)
  val genBsonLong: Gen[BsonLong] = arb[Long].map(BsonLong.apply)
  val genBsonDbPointer: Gen[BsonDbPointer] = for {
    namespace ← identifier
    id        ← genObjectId
  } yield BsonDbPointer(namespace, id)
  val genBsonString: Gen[BsonString] = arb[String].map(BsonString.apply)
  val genBsonJavaScript: Gen[BsonJavaScript] = arb[String].map(BsonJavaScript.apply)
  val genBsonJavaScriptWithScope: Gen[BsonJavaScriptWithScope] = for {
    js    ← arb[String]
    scope ← genBsonDocument(0)
  } yield BsonJavaScriptWithScope(js, scope.value)

  val genBsonSymbol: Gen[BsonSymbol] = arb[String].map(BsonSymbol.apply)
  val genBsonRegularExpression: Gen[BsonRegularExpression] = for {
    pattern ← oneOf("[a-zA-z]", "^[a-z0-9_-]{3,16}$", "^[a-z0-9_-]{6,18}$", "^#?([a-f0-9]{6}|[a-f0-9]{3})$")
    opts    ← genRegexOptions
  } yield BsonRegularExpression(Pattern.compile(pattern, opts))

  val genBsonTimestamp: Gen[BsonTimestamp] = for {
    seconds ← posNum[Int]
    inc     ← choose(0, 100)
  } yield BsonTimestamp(seconds, inc)
  val genBsonDateTime: Gen[BsonDateTime] = posNum[Long].map(BsonDateTime.apply)
  val genBsonUndefined: Gen[BsonUndefined.type] = const(BsonUndefined)
  val genBsonMaxKey: Gen[BsonMaxKey.type] = const(BsonMaxKey)
  val genBsonMinKey: Gen[BsonMinKey.type] = const(BsonMinKey)
  val genBsonNull: Gen[BsonNull.type] = const(BsonNull)

  val genValueType: Gen[BsonValue] = Gen.oneOf(genBsonBoolean, genBsonDouble, genBsonInt, genBsonLong,
      genBsonString, genBsonSymbol, genBsonTimestamp, genBsonDateTime, genBsonUndefined, genBsonMaxKey, genBsonMinKey,
      genBsonNull, genBsonObjectId, genBsonRegularExpression, genBsonDbPointer, genBsonDecimal128, genBsonBinaryData)

  val genJavascript: Gen[BsonValue] = Gen.oneOf(genBsonJavaScript, genBsonJavaScriptWithScope)

  val genTerminalType: Gen[BsonValue] = Gen.frequency(20 → genValueType, 1 → genJavascript)


  // - Nested types ----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def genBsonArray(depth: Int): Gen[BsonArray] = Gen.listOf(genBsonValue(depth)).map(BsonArray.apply)

  def genBsonDocument(depth: Int): Gen[BsonDocument] = Gen.mapOf(for {
    id    ← identifier
    value ← genBsonValue(depth)
  } yield id → value).map(BsonDocument.apply)
  implicit val arbBsonDocument: Arbitrary[BsonDocument] = Arbitrary(genBsonDocument(4))

  def genNestedType(depth: Int): Gen[BsonValue] = oneOf(genBsonDocument(depth), genBsonArray(depth))

  def genBsonValue(depth: Int): Gen[BsonValue] =
    if(depth <= 0) genValueType
    else           frequency((10, genTerminalType), (1, genNestedType(depth - 1)))
}
