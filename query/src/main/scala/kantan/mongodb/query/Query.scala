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

import kantan.mongodb._
import kantan.mongodb.query.Query.Compound.Or
import kantan.mongodb.query.QueryOperator.Eq
import sun.invoke.empty.Empty

sealed trait Query extends Product with Serializable

object Query {
  // - Helper functions ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def eq[A](field: String, value: A): Field[QueryOperator.Eq[A]] = Query.Field(field, QueryOperator.Eq(value))
  def ne[A](field: String, value: A): Not[Field[QueryOperator.Eq[A]]] = Not(Field(field, QueryOperator.Eq(value)))



  // - Empty -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  case object Empty extends Query {
    def &&[Q <: Query](q: Q): Q = q
    def ||[Q <: Query](q: Q): Q = q
    def unary_!(): Empty.type = Empty
  }

  implicit val emptyDocumentEncoder: BsonDocumentEncoder[Empty] = BsonDocumentEncoder.from(_ ⇒ BsonDocument.empty)



  // - Not -------------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Not[A](value: A) extends Query {
    def &&[Q <: Query](q: Q): Compound.And[Not[A], Q] = Compound.And(this, q)
    def ||[Q <: Query](q: Q): Compound.Or[Not[A], Q] = Compound.Or(this, q)
    def unary_!(): A = value
  }

  implicit def notNotEncoder[A: BsonValueEncoder]: BsonValueEncoder[Not[Not[A]]] =
    BsonValueEncoder[A].contramap { case (Not(Not(a))) ⇒ a }

  implicit def notFieldEncoder[A: BsonValueEncoder]: BsonDocumentEncoder[Not[Field[A]]] =
    BsonDocumentEncoder[Field[Not[A]]].contramap { case (Not(Field(name, a))) ⇒ Field(name, Not(a)) }

  implicit def notEqEncoder[A: BsonValueEncoder]: BsonDocumentEncoder[Not[Field[Eq[A]]]] = BsonDocumentEncoder.from {
    case Not(Field(name, QueryOperator.Eq(a))) ⇒ BsonDocument(Map(name → QueryOperator.encode("$ne", a))) }

  implicit def norEncoder[L, R](implicit f: Compound.Flattener[Or[L, R]]): BsonDocumentEncoder[Not[Or[L, R]]] =
      BsonDocumentEncoder.from { case Not(or) ⇒
          BsonDocument(Map("$nor" → BsonArray(f.flatten(or))))
      }

  implicit def notEncoder[A: BsonValueEncoder]: BsonDocumentEncoder[Not[A]] = BsonDocumentEncoder.from { case Not(a) ⇒
    QueryOperator.encode("$not", a)
  }


  // - Compound filters ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------

  sealed abstract class Compound[L, R](val operator: String, val left: L, val right: R) extends Query

  object Compound {
    def unapply[L, R](c: Compound[L, R]): Option[(String, L, R)] = Some((c.operator, c.left, c.right))

    final case class And[L, R](override val left: L, override val right: R) extends Compound("$and", left, right) {
      def &&[Q <: Query](q: Q): And[And[L, R], Q] = And(this, q)
      def ||[Q <: Query](q: Q): Or[And[L, R], Q] = Or(this, q)
      def unary_!(): Not[And[L, R]] = Not(this)
    }

    final case class Or[L, R](override val left: L, override val right: R) extends Compound("$or", left, right) {
      def &&[Q <: Query](q: Q): And[Q, Or[L, R]] = And(q, this)
      def ||[Q <: Query](q: Q): Or[Q, Or[L, R]] = Or(q, this)
      def unary_!(): Not[Or[L, R]] = Not(this)
    }

    trait Flattener[A] {
      def flatten(c: A): Seq[BsonDocument]
    }

    implicit def leftFlattener[C[X, Y] <: Compound[X, Y], L1, L2, R: BsonDocumentEncoder]
    (implicit f: Flattener[C[L1, L2]]): Flattener[C[C[L1, L2], R]] = new Flattener[C[C[L1, L2], R]] {
      override def flatten(c: C[C[L1, L2], R]) = f.flatten(c.left) :+ BsonDocumentEncoder[R].encode(c.right)
    }

    implicit def rightFlattener[C[X, Y] <: Compound[X, Y], L: BsonDocumentEncoder, R1, R2]
    (implicit f: Flattener[C[R1, R2]]): Flattener[C[L, C[R1, R2]]] = new Flattener[C[L, C[R1, R2]]] {
      override def flatten(c: C[L, C[R1, R2]]) = BsonDocumentEncoder[L].encode(c.left) +: f.flatten(c.right)
    }

    implicit def leftRightFlattener[C[X, Y] <: Compound[X, Y], L1, L2, R1, R2]
    (implicit fl: Flattener[C[L1, L2]], fr: Flattener[C[R1, R2]]): Flattener[C[C[L1, L2], C[R1, R2]]] =
      new Flattener[C[C[L1, L2], C[R1, R2]]] {
        override def flatten(c: C[C[L1, L2], C[R1, R2]]) = fl.flatten(c.left) ++ fr.flatten(c.right)
      }

    implicit def flattener[C[X, Y] <: Compound[X, Y], L: BsonDocumentEncoder, R: BsonDocumentEncoder]:
    Flattener[C[L, R]] = new Flattener[C[L, R]] {
      override def flatten(c: C[L, R]) = Seq(
        BsonDocumentEncoder[L].encode(c.left),
        BsonDocumentEncoder[R].encode(c.right)
      )
    }

    implicit def compoundDocumentEncoder[C[X, Y] <: Compound[X, Y], L, R](implicit f: Flattener[C[L, R]])
    : BsonDocumentEncoder[C[L, R]] = BsonDocumentEncoder.from { c ⇒
      BsonDocument(Map(c.operator → BsonArray(f.flatten(c))))
    }
  }



  // - Field-based filters ---------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Field[A](field: String, operator: A) extends Query {
    def &&[Q <: Query](q: Q): Query.Compound.And[Field[A], Q] = Query.Compound.And(this, q)
    def ||[Q <: Query](q: Q): Query.Compound.Or[Field[A], Q] = Query.Compound.Or(this, q)
    def unary_!(): Not[Field[A]] = Not(this)
  }

  implicit def fieldDocumentEncoder[A: BsonValueEncoder]: BsonDocumentEncoder[Field[A]] = BsonDocumentEncoder.from {
    case Field(name, value) ⇒ BsonDocument(Map(name → BsonValueEncoder[A].encode(value)))
  }
}
