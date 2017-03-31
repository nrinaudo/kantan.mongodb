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
import kantan.mongodb._
import kantan.mongodb.query.Query.Compound.{And, Nor, Or}
import kantan.mongodb.query.QueryOperator._

sealed trait Query extends Product with Serializable

object Query {
  // - Helper methods --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def all[A: BsonValueEncoder](field: String, as: A*): Query = Field(field, All(as))
  def and(filters: Query*): Query = And(filters)
  def bitsAllClear(field: String, mask: Long): Query = Field(field, Bits.AllClear(mask))
  def bitsAllSet(field: String, mask: Long): Query = Field(field, Bits.AllSet(mask))
  def bitsAnyClear(field: String, mask: Long): Query = Field(field, Bits.AnyClear(mask))
  def bitsAnySet(field: String, mask: Long): Query = Field(field, Bits.AnySet(mask))
  def elemMatch[A: BsonDocumentEncoder](field: String, filter: A): Query = Field(field, ElemMatch(filter))
  def eq[A: BsonValueEncoder](field: String, value: A): Query = Field(field, Eq(value))
  def exists(field: String, flag: Boolean): Query = Field(field, Exists(flag))
  def geoWithin(field: String, shape: Shape): Query = Field(field, Geo.Within(shape))
  def gt[A: BsonValueEncoder](field: String, value: A): Query = Field(field, Gt(value))
  def gte[A: BsonValueEncoder](field: String, value: A): Query = Field(field, Gte(value))
  def in[A: BsonValueEncoder](field: String, values: A*): Query = Field(field, In(values))
  def lt[A: BsonValueEncoder](field: String, value: A): Query = Field(field, Lt(value))
  def lte[A: BsonValueEncoder](field: String, value: A): Query = Field(field, Lte(value))
  def mod(field: String, divisor: Long, remainder: Long): Query = Field(field, Mod(Mod.Value(divisor, remainder)))
  def ne[A: BsonValueEncoder](field: String, value: A): Query = Field(field, Ne(value))
  def nin[A: BsonValueEncoder](field: String, values: A*): Query = Field(field, Nin(values))
  def none: Query = None
  def nor(filters: Query*): Query = Nor(filters)
  def not(filter: Query): Query = filter match {
    // None doesn't get negated.
    case None                     ⇒ None

    // Compound operators follow normal rules.
    case And(filters)             ⇒ Or(filters.map(not))
    case Or(filters)              ⇒ Nor(filters)
    case Nor(filters)             ⇒ Or(filters)

    // Eq and Ne are dual
    case Field(field, o@Eq(a))    ⇒ Field(field, Ne(a)(o.encoder))
    case Field(field, o@Ne(a))    ⇒ Field(field, Eq(a)(o.encoder))

    // Not is wrapped / unwrapped
    case Field(field, o@Not(operator))     ⇒ Field(field, operator)(o.encoder)
    case f@Field(field, operator) ⇒ Field(field, Not(operator)(f.encoder))
  }
  def or(filters: Query*): Query = Or(filters)
  def regex(field: String, pattern: Pattern): Query = Field(field, Regex(pattern))
  def size(field: String, size: Int): Query = Field(field, Size(size))
  def where(field: String, js: String): Query = Field(field, Where(js))


  // - Special filters -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  case object None extends Query


  // - Compound filters ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed abstract class Compound(val operator: String, val clauses: Seq[Query]) extends Query

  object Compound {
    def unapply(filter: Query): Option[(String, Seq[Query])] = filter match {
      case c: Compound ⇒ Some((c.operator, c.clauses))
      case _           ⇒ scala.None
    }
    final case class And(filters: Seq[Query]) extends Compound("$and", filters)
    final case class Or(filters: Seq[Query]) extends Compound("$or", filters)
    final case class Nor(filters: Seq[Query]) extends Compound("$nor", filters)
  }



  // - Field-based filters ---------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Field[A](field: String, operator: A)(implicit val encoder: BsonValueEncoder[A]) extends Query {
    private[mongodb] def encoded: BsonDocument = BsonDocument(Map(field → BsonValueEncoder[A].encode(operator)))
  }


  // - BSON encoding ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val queryDocumentEncoder: BsonDocumentEncoder[Query] = BsonDocumentEncoder.from {
    case None                  ⇒ BsonDocument.empty
    case f@Field(_, _)         ⇒ f.encoded

    // Empty compound queries have no representation.
    case Compound(_, Nil)      ⇒ BsonDocument.empty

    // Compound queries of exactly one value are represented as the filter itself.
    case Compound(_, f :: Nil) ⇒ BsonDocumentEncoder[Query].encode(f)

    // TODO: $and can and should be optimised
    // Normal case for compound queries.
    case Compound(op, filters) ⇒ BsonDocument(Map(op → BsonArray(filters.map(BsonDocumentEncoder[Query].encode))))
  }
}