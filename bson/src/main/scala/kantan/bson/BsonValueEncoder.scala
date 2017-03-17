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
import java.nio.file.Path
import java.util.UUID
import java.util.regex.Pattern
import kantan.codecs.Encoder
import kantan.codecs.strings.StringEncoder
import org.bson.types.ObjectId

object BsonValueEncoder {
  def apply[A](implicit ev: BsonValueEncoder[A]): BsonValueEncoder[A] = macro imp.summon[BsonValueEncoder[A]]

  def from[A](f: A ⇒ BsonValue): BsonValueEncoder[A] = Encoder.from(f)
}

trait LowPriorityBsonValueEncoderInstances {
  /** Turns any [[BsonDocumentEncoder]] instance into a [[BsonValueEncoder]] one. */
  implicit def encoderFromDocument[A: BsonDocumentEncoder]: BsonValueEncoder[A] =
    BsonValueEncoder.from(BsonDocumentEncoder[A].encode)
}

trait BsonValueEncoderInstances extends LowPriorityBsonValueEncoderInstances {
  /** Encodes `Int` values as [[BsonInt]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[Int].encode(12)
    * res0: BsonValue = BsonInt(12)
    * }}}
    */
  implicit val bsonIntEncoder: BsonValueEncoder[Int] = BsonValueEncoder.from(BsonInt.apply)

  /** Encodes `Long` values as [[BsonLong]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[Long].encode(12L)
    * res0: BsonValue = BsonLong(12)
    * }}}
    */
  implicit val bsonLongEncoder: BsonValueEncoder[Long] = BsonValueEncoder.from(BsonLong.apply)

  /** Encodes `Double` values as [[BsonDouble]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[Double].encode(0.5)
    * res0: BsonValue = BsonDouble(0.5)
    * }}}
    */
  implicit val bsonDoubleEncoder: BsonValueEncoder[Double] = BsonValueEncoder.from(BsonDouble.apply)

  /** Encodes `Boolean` values as [[BsonBoolean]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[Boolean].encode(true)
    * res0: BsonValue = BsonBoolean(true)
    * }}}
    */
  implicit val bsonBooleanEncoder: BsonValueEncoder[Boolean] = BsonValueEncoder.from(BsonBoolean.apply)

  /** Encodes `ObjectId` values as [[BsonObjectId]].
    *
    * For example:
    * {{{
    * scala> import org.bson.types._
    *
    * scala> BsonValueEncoder[ObjectId].encode(new ObjectId("58c64e6a54757ec4c09c525e"))
    * res0: BsonValue = BsonObjectId(58c64e6a54757ec4c09c525e)
    * }}}
    */
  implicit val bsonObjectIdEncoder: BsonValueEncoder[ObjectId] = BsonValueEncoder.from(BsonObjectId.apply)

  /** Encodes `Pattern` values as [[BsonRegularExpression]].
    *
    * For example:
    * {{{
    * scala> import java.util.regex.Pattern
    *
    * scala> BsonValueEncoder[Pattern].encode(Pattern.compile("[a-zA-Z]"))
    * res0: BsonValue = BsonRegularExpression([a-zA-Z])
    * }}}
    */
  implicit val bsonPatternEncoder: BsonValueEncoder[Pattern] = BsonValueEncoder.from(BsonRegularExpression.apply)

  /** Encodes `UUID` values as [[BsonUuid]].
    *
    * For example:
    * {{{
    * scala> import java.util.UUID
    *
    * scala> BsonValueEncoder[UUID].encode(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))
    * res0: BsonValue = BsonUuid(123e4567-e89b-12d3-a456-426655440000)
    * }}}
    */
  implicit val bsonUuidEncoder: BsonValueEncoder[UUID] = BsonValueEncoder.from(BsonUuid.apply)

  /** Encodes `Traversable` values as [[BsonArray]], provided the internal type has an instance of [[BsonValueEncoder]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[List[Int]].encode(List(1, 2, 3, 4))
    * res0: BsonValue = BsonArray(List(BsonInt(1), BsonInt(2), BsonInt(3), BsonInt(4)))
    * }}}
    */
  implicit def bsonArrayEncoder[C[X] <: Traversable[X], A: BsonValueEncoder]: BsonValueEncoder[C[A]] =
    BsonValueEncoder.from { values: C[A] ⇒
      BsonArray(values.foldLeft(Seq.newBuilder[BsonValue]) { (acc, a) ⇒ acc += BsonValueEncoder[A].encode(a) }.result())
    }

  /** Encodes `Option[A]` values as [[BsonValue]], provided there exists a `BsonEncoder[A]` in implicit scope.
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[Option[Boolean]].encode(None)
    * res0: BsonValue = BsonNull
    *
    * scala> BsonValueEncoder[Option[Boolean]].encode(Some(true))
    * res1: BsonValue = BsonBoolean(true)
    * }}}
    */
  implicit def bsonOptionEncoder[A: BsonValueEncoder]: BsonValueEncoder[Option[A]] = BsonValueEncoder.from {
    case Some(a) ⇒ BsonValueEncoder[A].encode(a)
    case None    ⇒ BsonNull
  }

  // - String-based Encoders -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Encodes `String` values as [[BsonString]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[String].encode("foobar")
    * res0: BsonValue = BsonString(foobar)
    * }}}
    */
  implicit val bsonStringEncoder: BsonValueEncoder[String] = BsonValueEncoder.from(BsonString.apply)

  def fromStringEncoder[A: StringEncoder]: BsonValueEncoder[A] =
    BsonValueEncoder.from(a ⇒ BsonString(StringEncoder[A].encode(a)))

  /** Encodes `URI` values as [[BsonString]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[java.net.URI].encode(new java.net.URI("http://localhost"))
    * res0: BsonValue = BsonString(http://localhost)
    * }}}
    */
  implicit val bsonUriEncoder: BsonValueEncoder[URI] = fromStringEncoder[URI]

  /** Encodes `URL` values as [[BsonString]].
    *
    * For example:
    * {{{
    * scala> BsonValueEncoder[java.net.URL].encode(new java.net.URL("http://localhost"))
    * res0: BsonValue = BsonString(http://localhost)
    * }}}
    */
  implicit val bsonUrlEncoder: BsonValueEncoder[URL] = fromStringEncoder[URL]

  /** Encodes `File` values as [[BsonString]].
    *
    * For example:
    * {{{
    * scala> import java.io._
    *
    * scala> BsonValueEncoder[File].encode(new File("/var/log"))
    * res0: BsonValue = BsonString(/var/log)
    * }}}
    */
  implicit val bsonFileEncoder: BsonValueEncoder[File] = fromStringEncoder[File]

  /** Encodes `Path` values as [[BsonString]].
    *
    * For example:
    * {{{
    * scala> import java.nio.file._
    *
    * scala> BsonValueEncoder[Path].encode(Paths.get("/var/log"))
    * res0: BsonValue = BsonString(/var/log)
    * }}}
    */
  implicit val bsonPathEncoder: BsonValueEncoder[Path] = fromStringEncoder[Path]
}
