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
import kantan.mongodb.{BsonDocument, BsonValue, BsonValueEncoder}

sealed abstract class QueryOperator[A](val operator: String, val operand: A)(implicit val encoder: BsonValueEncoder[A])
  extends Product with Serializable {
  def encodedOperand: BsonValue = BsonValueEncoder[A].encode(operand)
}

object QueryOperator {
  implicit def operatorValueEncoder[A <: QueryOperator[_]]: BsonValueEncoder[A] = BsonValueEncoder.from {
    case o@Eq(_)             ⇒ o.encodedOperand
    case o@Regex(_)          ⇒ o.encodedOperand
    case o: QueryOperator[_] ⇒ BsonDocument(Map(o.operator → o.encodedOperand))
  }

  final case class Eq[A: BsonValueEncoder](value: A) extends QueryOperator("$eq", value)
  final case class Not[A: BsonValueEncoder](value: A) extends QueryOperator("$not", value)
  final case class Ne[A: BsonValueEncoder](value: A) extends QueryOperator("$ne", value)
  final case class Gt[A: BsonValueEncoder](value: A) extends QueryOperator("$gt", value)
  final case class Gte[A: BsonValueEncoder](value: A) extends QueryOperator("$gte", value)
  final case class Lt[A: BsonValueEncoder](value: A) extends QueryOperator("$lt", value)
  final case class Lte[A: BsonValueEncoder](value: A) extends QueryOperator("$lte", value)
  final case class In[A: BsonValueEncoder](values: Seq[A]) extends QueryOperator("$in", values)
  final case class Nin[A: BsonValueEncoder](values: Seq[A]) extends QueryOperator("$nin", values)
  final case class All[A: BsonValueEncoder](values: Seq[A]) extends QueryOperator("$all", values)
  final case class ElemMatch[A: BsonValueEncoder](filter: A) extends QueryOperator("$elemMatch", filter)
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

  sealed trait Bits
  object Bits {
    final case class AllClear(mask: Long) extends QueryOperator("$bitsAllClear", mask) with Bits
    final case class AllSet(mask: Long) extends QueryOperator("$bitsAllSet", mask) with Bits
    final case class AnyClear(mask: Long) extends QueryOperator("$bitsAnyClear", mask) with Bits
    final case class AnySet(mask: Long) extends QueryOperator("$bitsAnySet", mask) with Bits
  }

  sealed trait Geo
  object Geo {
    // TODO: near
    final case class Intersects[A: BsonValueEncoder](value: A) extends QueryOperator("$geoIntersects", value) with Geo
    final case class Within[A: BsonValueEncoder](value: A) extends QueryOperator("$geoWithin", value) with Geo
  }
}