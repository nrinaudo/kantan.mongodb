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

import java.io.File
import java.net.{URI, URL}
import java.nio.file.Path
import java.util.{Date, UUID}
import java.util.regex.Pattern
import kantan.codecs.Decoder
import kantan.codecs.Result.{Failure, Success}
import kantan.codecs.strings.StringDecoder
import org.bson.types.ObjectId
import scala.collection.generic.CanBuildFrom

object BsonValueDecoder {
  def apply[A](implicit ev: BsonValueDecoder[A]): BsonValueDecoder[A] = macro imp.summon[BsonValueDecoder[A]]

  def fromSafe[A](f: PartialFunction[BsonValue, A]): BsonValueDecoder[A] =
    BsonValueDecoder.from(f andThen Success.apply)

  def from[A](f: PartialFunction[BsonValue, DecodeResult[A]]): BsonValueDecoder[A] = Decoder.from { value ⇒
    if(f.isDefinedAt(value)) f(value)
    else                     Failure(MongoError.Decode(s"unexpected BSON value: $value"))
  }
}

trait LowPriorityBsonValueDecoderInstances {
  implicit def decoderFromDocument[A: BsonDocumentDecoder]: BsonValueDecoder[A] = BsonValueDecoder.from {
    case a@BsonDocument(_) ⇒ BsonDocumentDecoder[A].decode(a)
  }
}

trait BsonValueDecoderInstances extends LowPriorityBsonValueDecoderInstances {
  /** Decodes instances of [[BsonInt]], [[BsonMaxKey]] and [[BsonMinKey]] as `Int`.
    *
    * For example:
    * {{{
    * scala> BsonValueDecoder[Int].decode(BsonInt(1))
    * res0: DecodeResult[Int] = Success(1)
    * }}}
    *
    * scala> BsonValueDecoder[Int].decode(BsonMinKey)
    * res1: DecodeResult[Int] = Success(-2147483648)
    *
    * scala> BsonValueDecoder[Int].decode(BsonMaxKey)
    * res2: DecodeResult[Int] = Success(2147483647)
    */
  implicit val bsonIntDecoder: BsonValueDecoder[Int] = BsonValueDecoder.fromSafe {
    case BsonInt(i) ⇒ i
    case BsonMaxKey ⇒ Int.MaxValue
    case BsonMinKey ⇒ Int.MinValue
  }

  /** Decodes instances of [[BsonLong]], [[BsonMaxKey]] and [[BsonMinKey]] as `Long`.
    *
    * For example:
    * {{{
    * scala> BsonValueDecoder[Long].decode(BsonLong(1L))
    * res0: DecodeResult[Long] = Success(1)
    * }}}
    *
    * scala> BsonValueDecoder[Long].decode(BsonMinKey)
    * res1: DecodeResult[Long] = Success(-9223372036854775808)
    *
    * scala> BsonValueDecoder[Long].decode(BsonMaxKey)
    * res2: DecodeResult[Long] = Success(9223372036854775807)
    */
  implicit val bsonLongDecoder: BsonValueDecoder[Long] = BsonValueDecoder.fromSafe {
    case BsonLong(l) ⇒ l
    case BsonMaxKey ⇒ Long.MaxValue
    case BsonMinKey ⇒ Long.MinValue
  }

  /** Decodes instances of [[BsonDouble]], [[BsonMaxKey]] and [[BsonMinKey]] as `Double`.
    *
    * For example:
    * {{{
    * scala> BsonValueDecoder[Double].decode(BsonDouble(0.5))
    * res0: DecodeResult[Double] = Success(0.5)
    * }}}
    *
    * scala> BsonValueDecoder[Double].decode(BsonMinKey)
    * res1: DecodeResult[Double] = Success(-1.7976931348623157E308)
    *
    * scala> BsonValueDecoder[Double].decode(BsonMaxKey)
    * res2: DecodeResult[Double] = Success(1.7976931348623157E308)
    */
  implicit val bsonDoubleDecoder: BsonValueDecoder[Double] = BsonValueDecoder.fromSafe {
    case BsonDouble(d) ⇒ d
    case BsonMaxKey ⇒ Double.MaxValue
    case BsonMinKey ⇒ Double.MinValue
  }

  /** Decodes instances of [[BsonObjectId]] as `ObjectId`.
    *
    * For example:
    * {{{
    * scala> import org.bson.types._
    *
    * scala> BsonValueDecoder[ObjectId].decode(BsonObjectId(new ObjectId("58c64e6a54757ec4c09c525e")))
    * res0: DecodeResult[ObjectId] = Success(58c64e6a54757ec4c09c525e)
    * }}}
    */
  implicit val bsonObjectIdDecoder: BsonValueDecoder[ObjectId] = BsonValueDecoder.fromSafe {
    case BsonObjectId(i) ⇒ i
  }

  /** Decodes instances of [[BsonBoolean]] as `Boolean`.
    *
    * For example:
    * {{{
    * scala> BsonValueDecoder[Boolean].decode(BsonBoolean(true))
    * res0: DecodeResult[Boolean] = Success(true)
    * }}}
    */
  implicit val bsonBooleanDecoder: BsonValueDecoder[Boolean] = BsonValueDecoder.fromSafe {
    case BsonBoolean(b) ⇒ b
  }

  /** Decodes instances of [[BsonRegularExpression]] as `Pattern`.
    *
    * For example:
    * {{{
    * scala> import java.util.regex._
    *
    * scala> BsonValueDecoder[Pattern].decode(BsonRegularExpression(Pattern.compile("[a-zA-Z]*")))
    * res0: DecodeResult[Pattern] = Success([a-zA-Z]*)
    * }}}
    */
  implicit val bsonRegularExpressionDecoder: BsonValueDecoder[Pattern] = BsonValueDecoder.fromSafe {
    case BsonRegularExpression(p) ⇒ p
  }

  /** Decodes instances of [[BsonUuid]] as `UUID`.
    *
    * For example:
    * {{{
    * scala> import java.util._
    *
    * scala> BsonValueDecoder[UUID].decode(BsonUuid(UUID.fromString("123e4567-e89b-12d3-a456-426655440000")))
    * res0: DecodeResult[UUID] = Success(123e4567-e89b-12d3-a456-426655440000)
    * }}}
    */
  implicit val bsonUuidDecoder: BsonValueDecoder[UUID] = BsonValueDecoder.from {
    case BsonUuid(uuid)  ⇒ DecodeResult.success(uuid)
  }

  /** Decodes instances of `BsonArray` as `C[A]`, provided `C` as a `CanBuildFrom` and `A` a [[BsonValueDecoder]].
    *
    * For example:
    * {{{
    * scala> BsonValueDecoder[List[Int]].decode(BsonArray(Seq(BsonInt(1), BsonInt(2), BsonInt(3))))
    * res0> DecodeResult[List[Int]] = Success(List(1, 2, 3))
    * }}}
    */
  implicit def bsonArrayDecoder[C[_], A: BsonValueDecoder]
  (implicit cbf: CanBuildFrom[Nothing, A, C[A]]): BsonValueDecoder[C[A]] = BsonValueDecoder.from {
    case BsonArray(values) ⇒ values.foldLeft(DecodeResult(cbf.apply())) { (racc, v) ⇒ for {
      acc ← racc
      a   ← BsonValueDecoder[A].decode(v)
    } yield acc += a
    }.map(_.result())
  }

  /** Decodes instances of [[BsonNull]] as `None` and [[BsonValue]] as `Some(A)`, provided there exists a
    * `BsonValueDecoder[A]` in implicit scope.
    *
    * For example:
    * {{{
    * scala> BsonValueDecoder[Option[Int]].decode(BsonNull)
    * res0> DecodeResult[Option[Int]] = None
    *
    * scala> BsonValueDecoder[Option[Int]].decode(BsonInt(1))
    * res0> DecodeResult[Option[Int]] = Some(1)
    * }}}
    */
  implicit def bsonOptionDecoder[A: BsonValueDecoder]: BsonValueDecoder[Option[A]] = BsonValueDecoder.from {
    case BsonNull ⇒ DecodeResult.success(None)
    case value    ⇒ BsonValueDecoder[A].decode(value).map(Some.apply)
  }

  implicit val javaUtilDateDecoder: BsonValueDecoder[Date] = BsonValueDecoder.fromSafe {
    case BsonDateTime(i) ⇒ new Date(i)
  }



  // - String-based decoders -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Decodes instances of [[BsonString]] as `String`.
    *
    * For example:
    * {{{
    * scala> BsonValueDecoder[String].decode(BsonString("foobar"))
    * res0: DecodeResult[String] = Success(foobar)
    * }}}
    */
  implicit val bsonStringDecoder: BsonValueDecoder[String] = BsonValueDecoder.fromSafe {
    case BsonString(s) ⇒ s
  }

  /** Creates a new [[BsonValueDecoder]] from the specified  `StringDecoder`.
    *
    * Note that the resulting decoder will only work on values of type [[BsonString]].
    */
  def fromStringDecoder[A: StringDecoder]: BsonValueDecoder[A] =
    BsonValueDecoder[String].mapResult(s ⇒ StringDecoder[A].decode(s).leftMap(e ⇒ MongoError.Decode(e.message)))

  /** Decodes instances of [[BsonString]] as `URI`.
    *
    * For example:
    * {{{
    * scala> import java.net._
    *
    * scala> BsonValueDecoder[URI].decode(BsonString("http://localhost"))
    * res0: DecodeResult[URI] = Success(http://localhost)
    * }}}
    */
  implicit val bsonUriDecoder: BsonValueDecoder[URI] = fromStringDecoder[URI]

  /** Decodes instances of [[BsonString]] as `URL`.
    *
    * For example:
    * {{{
    * scala> import java.net._
    *
    * scala> BsonValueDecoder[URL].decode(BsonString("http://localhost"))
    * res0: DecodeResult[URL] = Success(http://localhost)
    * }}}
    */
  implicit val bsonUrlDecoder: BsonValueDecoder[URL] = fromStringDecoder[URL]

  /** Decodes instances of [[BsonString]] as `File`.
    *
    * For example:
    * {{{
    * scala> import java.io._
    *
    * scala> BsonValueDecoder[File].decode(BsonString("/var/log"))
    * res0: DecodeResult[File] = Success(/var/log)
    * }}}
    */
  implicit val bsonFileDecoder: BsonValueDecoder[File] = fromStringDecoder[File]

  /** Decodes instances of [[BsonString]] as `Path`.
    *
    * For example:
    * {{{
    * scala> import java.nio.file._
    *
    * scala> BsonValueDecoder[Path].decode(BsonString("/var/log"))
    * res0: DecodeResult[Path] = Success(/var/log)
    * }}}
    */
  implicit val bsonPathDecoder: BsonValueDecoder[Path] = fromStringDecoder[Path]

  // The following BSON types don't currently have default support because I'm not sure what to do with them:
  // - BsonUserDefinedBinary
  // - BsonFunction
  // - BsonMd5
  // - BsonBinary
  // - BsonJavaScript
  // - BsonJavaScriptWithScope
  // - BsonDbPointer

  // Additionally:
  // - BsonUndefined is not supported - maybe it could be an Option?
  // - BsonSymbol is not supported because it's deprecated.

  // TODO:
  // - BsonDocument
  // - BsonDecimal128
  // - BsonTimestamp
}
