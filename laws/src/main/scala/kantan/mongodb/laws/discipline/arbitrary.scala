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

package kantan.mongodb.laws.discipline

import imp.imp
import java.security.MessageDigest
import java.util.UUID
import java.util.regex.Pattern
import kantan.mongodb._
import org.bson.types.{Decimal128, ObjectId}
import org.scalacheck._, Arbitrary.{arbitrary ⇒ arb}, Gen._
import org.scalacheck.rng.Seed

object arbitrary extends ArbitraryInstances

trait ArbitraryInstances extends kantan.codecs.laws.discipline.ArbitraryInstances {
  // - BSON types ------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  val genObjectId: Gen[ObjectId] = for {
    ts      ← posNum[Int]
    machine ← posNum[Int]
    process ← posNum[Short]
    counter ← posNum[Int]
  } yield new ObjectId(ts, machine, process, counter)

  val genDecimal128: Gen[Decimal128] = arb[Long].map(l ⇒ new Decimal128(l))

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
  val genBsonObjectId: Gen[BsonObjectId]     = genObjectId.map(BsonObjectId.apply)
  val genBsonDecimal128: Gen[BsonDecimal128] = genDecimal128.map(BsonDecimal128.apply)
  val genBsonBoolean: Gen[BsonBoolean]       = arb[Boolean].map(BsonBoolean.apply)
  val genBsonDouble: Gen[BsonDouble]         = arb[Double].map(BsonDouble.apply)
  val genBsonInt: Gen[BsonInt]               = arb[Int].map(BsonInt.apply)
  val genBsonLong: Gen[BsonLong]             = arb[Long].map(BsonLong.apply)
  val genBsonDbPointer: Gen[BsonDbPointer] = for {
    namespace ← identifier
    id        ← genObjectId
  } yield BsonDbPointer(namespace, id)
  val genBsonString: Gen[BsonString]         = arb[String].map(BsonString.apply)
  val genBsonJavaScript: Gen[BsonJavaScript] = arb[String].map(BsonJavaScript.apply)
  val genBsonJavaScriptWithScope: Gen[BsonJavaScriptWithScope] = for {
    js    ← arb[String]
    scope ← genBsonDocument(0)
  } yield BsonJavaScriptWithScope(js, scope.value)

  val genBsonSymbol: Gen[BsonSymbol]                       = arb[String].map(BsonSymbol.apply)
  val genBsonRegularExpression: Gen[BsonRegularExpression] = genPattern.map(BsonRegularExpression.apply _)

  val genBsonTimestamp: Gen[BsonTimestamp] = for {
    seconds ← posNum[Int]
    inc     ← choose(0, 100)
  } yield BsonTimestamp(seconds, inc)
  val genBsonDateTime: Gen[BsonDateTime]        = posNum[Long].map(BsonDateTime.apply)
  val genBsonUndefined: Gen[BsonUndefined.type] = const(BsonUndefined)
  val genBsonMaxKey: Gen[BsonMaxKey.type]       = const(BsonMaxKey)
  val genBsonMinKey: Gen[BsonMinKey.type]       = const(BsonMinKey)
  val genBsonNull: Gen[BsonNull.type]           = const(BsonNull)

  val genValueType: Gen[BsonValue] = Gen.oneOf(
    genBsonBoolean,
    genBsonDouble,
    genBsonInt,
    genBsonLong,
    genBsonString,
    genBsonSymbol,
    genBsonTimestamp,
    genBsonDateTime,
    genBsonUndefined,
    genBsonMaxKey,
    genBsonMinKey,
    genBsonNull,
    genBsonObjectId,
    genBsonRegularExpression,
    genBsonDbPointer,
    genBsonDecimal128,
    genBsonBinaryData
  )

  val genJavascript: Gen[BsonValue] = Gen.oneOf(genBsonJavaScript, genBsonJavaScriptWithScope)

  val genTerminalType: Gen[BsonValue] = Gen.frequency(20 → genValueType, 1 → genJavascript)

  // - Nested types ----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def genBsonArray(depth: Int): Gen[BsonArray] = Gen.listOf(genBsonValue(depth)).map(BsonArray.apply)

  def genBsonDocument(depth: Int): Gen[BsonDocument] =
    Gen
      .mapOf(for {
        id    ← identifier
        value ← genBsonValue(depth)
      } yield id → value)
      .map(BsonDocument.apply)

  def genNestedType(depth: Int): Gen[BsonValue] = oneOf(genBsonDocument(depth), genBsonArray(depth))

  def genBsonValue(depth: Int): Gen[BsonValue] =
    if(depth <= 0) genValueType
    else frequency((10, genTerminalType), (1, genNestedType(depth - 1)))

  // - Arbitrary instances ---------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val arbDecodeError: Arbitrary[MongoError.Decode] =
    Arbitrary(genException.map(MongoError.Decode.apply))
  implicit val arbBsonDocument: Arbitrary[BsonDocument] = Arbitrary(genBsonDocument(4))
  implicit val arbBsonValue: Arbitrary[BsonValue]       = Arbitrary(genBsonValue(4))
  implicit val arbObjectId: Arbitrary[ObjectId]         = Arbitrary(genObjectId)

  // - Cogen instances -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val cogenObjectId: Cogen[ObjectId] = imp[Cogen[String]].contramap(_.toString)

  implicit val cogenBsonDecodeError: Cogen[MongoError.Decode] =
    Cogen((seed: Seed, err: MongoError.Decode) ⇒ imp[Cogen[String]].perturb(seed, err.message))

  implicit val cogenBsonDocumentContent: Cogen[Map[String, BsonValue]] =
    Cogen.it(_.toVector.sortBy(_._1).iterator)

  implicit val cogenBsonDocument: Cogen[BsonDocument] = cogenBsonDocumentContent.contramap(_.value)

  //implicit val cogenPattern: Cogen[Pattern] = imp[Cogen[(String, Int)]].contramap(p ⇒ (p.pattern(), p.flags()))

  implicit lazy val bsonValueCogen: Cogen[BsonValue] = Cogen(
    (seed: Seed, a: BsonValue) ⇒
      a match {
        case BsonUuid(uuid)                    ⇒ imp[Cogen[UUID]].perturb(seed, uuid)
        case BsonMd5(md5)                      ⇒ imp[Cogen[String]].perturb(seed, md5)
        case BsonUserDefinedBinary(value)      ⇒ imp[Cogen[Seq[Byte]]].perturb(seed, value)
        case BsonFunction(value)               ⇒ imp[Cogen[Seq[Byte]]].perturb(seed, value)
        case BsonBinary(value)                 ⇒ imp[Cogen[Seq[Byte]]].perturb(seed, value)
        case BsonArray(value)                  ⇒ imp[Cogen[Seq[BsonValue]]].perturb(seed, value)
        case BsonDocument(value)               ⇒ cogenBsonDocumentContent.perturb(seed, value)
        case BsonJavaScriptWithScope(value, s) ⇒ imp[Cogen[(String, Map[String, BsonValue])]].perturb(seed, (value, s))
        case BsonMaxKey                        ⇒ seed
        case BsonMinKey                        ⇒ seed
        case BsonNull                          ⇒ seed
        case BsonUndefined                     ⇒ seed
        case BsonBoolean(value)                ⇒ imp[Cogen[Boolean]].perturb(seed, value)
        case BsonDateTime(value)               ⇒ imp[Cogen[Long]].perturb(seed, value)
        case BsonDbPointer(namespace, id)      ⇒ imp[Cogen[(String, String)]].perturb(seed, (namespace, id.toString))
        case BsonDecimal128(value)             ⇒ imp[Cogen[(Long, Long)]].perturb(seed, (value.getLow, value.getHigh))
        case BsonJavaScript(value)             ⇒ imp[Cogen[String]].perturb(seed, value)
        case BsonDouble(value)                 ⇒ imp[Cogen[Double]].perturb(seed, value)
        case BsonInt(value)                    ⇒ imp[Cogen[Int]].perturb(seed, value)
        case BsonLong(value)                   ⇒ imp[Cogen[Long]].perturb(seed, value)
        case BsonObjectId(value)               ⇒ imp[Cogen[ObjectId]].perturb(seed, value)
        case BsonString(value)                 ⇒ imp[Cogen[String]].perturb(seed, value)
        case BsonSymbol(value)                 ⇒ imp[Cogen[String]].perturb(seed, value)
        case BsonTimestamp(seconds, inc)       ⇒ imp[Cogen[(Int, Int)]].perturb(seed, (seconds, inc))
        case BsonRegularExpression(value)      ⇒ imp[Cogen[Pattern]].perturb(seed, value)
    }
  )
}
