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

package kantan.mongodb.query

import java.util.regex.Pattern
import kantan.mongodb.{BsonDocument, BsonValueEncoder}

sealed abstract class QueryOperator[A](val operator: String, val operand: A) extends Product with Serializable

object QueryOperator {
  def unapply[A](op: QueryOperator[A]): Option[(String, A)] = Some((op.operator, op.operand))

  final case class Eq[A](value: A) extends QueryOperator("$eq", value)
  final case class Gt[A](value: A) extends QueryOperator("$gt", value)
  final case class Gte[A](value: A) extends QueryOperator("$gte", value)
  final case class Lt[A](value: A) extends QueryOperator("$lt", value)
  final case class Lte[A](value: A) extends QueryOperator("$lte", value)
  final case class In[A](values: Seq[A]) extends QueryOperator("$in", values)
  final case class All[A](values: Seq[A]) extends QueryOperator("$all", values)
  final case class ElemMatch[A](filter: A) extends QueryOperator("$elemMatch", filter)
  final case class Exists(flag: Boolean) extends QueryOperator("$exists", flag)
  final case class Regex(value: Pattern) extends QueryOperator("$regex", value)
  final case class Size(value: Int) extends QueryOperator("$size", value)
  final case class Where(value: String) extends QueryOperator("$where", value)
  final case class Mod(value: Mod.Value) extends QueryOperator("$mod", value)
  object Mod {
    final case class Value(divisor: Long, remainder: Long)
    implicit val valueEncoder: BsonValueEncoder[Value] =
      BsonValueEncoder[List[Long]].contramap(v ⇒ List(v.divisor, v.remainder))
  }

  sealed abstract class Bits(op: String, mask: Long) extends QueryOperator(op, mask)
  object Bits {
    def unapply(op: Bits): Option[(String, Long)] = Some((op.operator, op.operand))
    final case class AllClear(mask: Long) extends Bits("$bitsAllClear", mask)
    final case class AllSet(mask: Long) extends Bits("$bitsAllSet", mask)
    final case class AnyClear(mask: Long) extends Bits("$bitsAnyClear", mask)
    final case class AnySet(mask: Long) extends Bits("$bitsAnySet", mask)
  }

  sealed trait Geo
  object Geo {
    final case class Intersects[A](value: A) extends QueryOperator("$geoIntersects", value) with Geo
    final case class Within[A](value: A) extends QueryOperator("$geoWithin", value) with Geo
  }

  // TODO: type, near, text

  // - BSON encoding ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  private[query] def encode[A: BsonValueEncoder](operator: String, operand: A): BsonDocument =
    BsonDocument(Map(operator → BsonValueEncoder[A].encode(operand)))

  implicit val existsValueEncoder: BsonValueEncoder[Exists] =
    BsonValueEncoder[QueryOperator[Boolean]].contramap(identity)

  implicit val regexEncoder: BsonValueEncoder[Regex] = BsonValueEncoder[QueryOperator[Pattern]].contramap(identity)

  implicit val sizeEncoder: BsonValueEncoder[Size] = BsonValueEncoder[QueryOperator[Int]].contramap(identity)

  implicit val whereEncoder: BsonValueEncoder[Where] = BsonValueEncoder[QueryOperator[String]].contramap(identity)

  implicit val modEncoder: BsonValueEncoder[Mod] = BsonValueEncoder[QueryOperator[Mod.Value]].contramap(identity)

  implicit def seqOperatorEncoder[Q[X] <: QueryOperator[Seq[X]], A: BsonValueEncoder]: BsonValueEncoder[Q[A]] =
    BsonValueEncoder[QueryOperator[Seq[A]]].contramap(identity)

  implicit def bitsOperatorEncoder[Q <: Bits]: BsonValueEncoder[Q] =
    BsonValueEncoder[QueryOperator[Long]].contramap(identity)

  implicit def simpleOperatorEncoder[Q[X] <: QueryOperator[X], A: BsonValueEncoder]: BsonValueEncoder[Q[A]] =
    BsonValueEncoder.from { case QueryOperator(op, a) ⇒ encode(op, a) }

  implicit def eqValueEncoder[A: BsonValueEncoder]: BsonValueEncoder[Eq[A]] =
    BsonValueEncoder[A].contramap { case Eq(a) ⇒ a }
}
