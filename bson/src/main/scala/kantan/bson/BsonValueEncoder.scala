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
import org.bson.types.ObjectId

object BsonValueEncoder {
  def apply[A](implicit ev: BsonValueEncoder[A]): BsonValueEncoder[A] = macro imp.summon[BsonValueEncoder[A]]

  def from[A](f: A ⇒ BsonValue): BsonValueEncoder[A] = Encoder.from(f)
}

trait BsonValueEncoderInstances {
  implicit val bsonIntEncoder: BsonValueEncoder[Int] = BsonValueEncoder.from(BsonInt.apply)
  implicit val bsonLongEncoder: BsonValueEncoder[Long] = BsonValueEncoder.from(BsonLong.apply)
  implicit val bsonDoubleEncoder: BsonValueEncoder[Double] = BsonValueEncoder.from(BsonDouble.apply)
  implicit val bsonObjectIdEncoder: BsonValueEncoder[ObjectId] = BsonValueEncoder.from(BsonObjectId.apply)
  implicit val bsonBooleanEncoder: BsonValueEncoder[Boolean] = BsonValueEncoder.from(BsonBoolean.apply)
  implicit val bsonPatternEncoder: BsonValueEncoder[Pattern] = BsonValueEncoder.from(BsonRegularExpression.apply)
  implicit val bsonUuidEncoder: BsonValueEncoder[UUID] = BsonValueEncoder.from(BsonUuid.apply)

  implicit def bsonArrayEncoder[C[X] <: Traversable[X], A: BsonValueEncoder]: BsonValueEncoder[C[A]] =
    BsonValueEncoder.from { values: C[A] ⇒
      BsonArray(values.foldLeft(Seq.newBuilder[BsonValue]) { (acc, a) ⇒ acc += BsonValueEncoder[A].encode(a) }.result())
    }

  implicit def bsonOptionEncoder[A: BsonValueEncoder]: BsonValueEncoder[Option[A]] = BsonValueEncoder.from {
    case Some(a) ⇒ BsonValueEncoder[A].encode(a)
    case None    ⇒ BsonNull
  }

  // - String-based Encoders -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val bsonStringEncoder: BsonValueEncoder[String] = BsonValueEncoder.from(BsonString.apply)
  implicit val bsonUriEncoder: BsonValueEncoder[URI] = BsonValueEncoder[String].contramap(_.toString)
  implicit val bsonUrlEncoder: BsonValueEncoder[URL] = BsonValueEncoder[String].contramap(_.toString)
  implicit val bsonFileEncoder: BsonValueEncoder[File] = BsonValueEncoder[String].contramap(_.toString)
  implicit val bsonPathEncoder: BsonValueEncoder[Path] = BsonValueEncoder[String].contramap(_.toString)
}