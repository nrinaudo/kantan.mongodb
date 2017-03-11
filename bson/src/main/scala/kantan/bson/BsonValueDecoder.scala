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

import java.io.File
import java.net.{URI, URL}
import java.nio.file.{Path, Paths}
import java.util.UUID
import java.util.regex.Pattern
import kantan.codecs.Decoder
import kantan.codecs.Result.{Failure, Success}
import org.bson.types.ObjectId
import scala.collection.generic.CanBuildFrom

object BsonValueDecoder {
  def apply[A](implicit ev: BsonValueDecoder[A]): BsonValueDecoder[A] = macro imp.summon[BsonValueDecoder[A]]

  def fromSafe[A](f: PartialFunction[BsonValue, A]): BsonValueDecoder[A] =
    BsonValueDecoder.from(f andThen Success.apply)

  def from[A](f: PartialFunction[BsonValue, DecodeResult[A]]): BsonValueDecoder[A] = Decoder.from { value ⇒
    if(f.isDefinedAt(value)) f(value)
    else                     Failure(DecodeError(s"unexpected BSON value: $value"))
  }
}

trait BsonValueDecoderInstances {
  implicit val bsonIntDecoder: BsonValueDecoder[Int] = BsonValueDecoder.fromSafe {
    case BsonInt(i) ⇒ i
    case BsonMaxKey ⇒ Int.MaxValue
    case BsonMinKey ⇒ Int.MinValue
  }

  implicit val bsonLongDecoder: BsonValueDecoder[Long] = BsonValueDecoder.fromSafe {
    case BsonLong(l) ⇒ l
    case BsonMaxKey ⇒ Long.MaxValue
    case BsonMinKey ⇒ Long.MinValue
  }

  implicit val bsonDoubleDecoder: BsonValueDecoder[Double] = BsonValueDecoder.fromSafe {
    case BsonDouble(d) ⇒ d
    case BsonMaxKey ⇒ Double.MaxValue
    case BsonMinKey ⇒ Double.MinValue
  }

  implicit val bsonObjectIdDecoder: BsonValueDecoder[ObjectId] = BsonValueDecoder.fromSafe {
    case BsonObjectId(i) ⇒ i
  }

  implicit val bsonBooleanDecoder: BsonValueDecoder[Boolean] = BsonValueDecoder.fromSafe {
    case BsonBoolean(b) ⇒ b
  }

  implicit val bsonRegularExpressionDecoder: BsonValueDecoder[Pattern] = BsonValueDecoder.fromSafe {
    case BsonRegularExpression(p) ⇒ p
  }

  implicit val bsonUuidDecoder: BsonValueDecoder[UUID] = BsonValueDecoder.from {
    case BsonUuid(uuid)  ⇒ DecodeResult.success(uuid)
    case BsonString(str) ⇒ DecodeResult(UUID.fromString(str))
  }

  implicit def bsonArrayDecoder[C[_], A: BsonValueDecoder]
  (implicit cbf: CanBuildFrom[Nothing, A, C[A]]): BsonValueDecoder[C[A]] = BsonValueDecoder.from {
    case BsonArray(values) ⇒ values.foldLeft(DecodeResult(cbf.apply())) { (racc, v) ⇒ for {
      acc ← racc
      a   ← BsonValueDecoder[A].decode(v)
     } yield acc += a
    }.map(_.result())
  }

  implicit def bsonOptionDecoder[A: BsonValueDecoder]: BsonValueDecoder[Option[A]] = BsonValueDecoder.from {
    case BsonNull ⇒ DecodeResult.success(None)
    case value    ⇒ BsonValueDecoder[A].decode(value).map(Some.apply)
  }

  // - String-based decoders -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val bsonStringDecoder: BsonValueDecoder[String] = BsonValueDecoder.fromSafe {
    case BsonString(s) ⇒ s
  }

  implicit val bsonUriDecoder: BsonValueDecoder[URI] =
    BsonValueDecoder[String].mapResult(s ⇒ DecodeResult(new URI(s.trim)))

  implicit val bsonUrlDecoder: BsonValueDecoder[URL] =
    BsonValueDecoder[String].mapResult(s ⇒ DecodeResult(new URL(s.trim)))

  implicit val bsonFileDecoder: BsonValueDecoder[File] =
    BsonValueDecoder[String].mapResult(s ⇒ DecodeResult(new File(s.trim)))

  implicit val bsonPathDecoder: BsonValueDecoder[Path] =
    BsonValueDecoder[String].mapResult(s ⇒ DecodeResult(Paths.get(s.trim)))

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
  // - BsonDateTime
  // - BsonDecimal128
  // - BsonTimestamp
}
