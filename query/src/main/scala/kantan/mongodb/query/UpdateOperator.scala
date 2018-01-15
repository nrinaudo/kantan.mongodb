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
package query

sealed abstract class UpdateOperator[A](val operator: String, val operand: A) extends Product with Serializable

object UpdateOperator {
  def unapply[A](u: UpdateOperator[A]): Option[(String, A)] = Some((u.operator, u.operand))

  // - Array manipulation ----------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class AddToSet[A](value: A) extends UpdateOperator("$addToSet", value)
  case object PopFirst                   extends UpdateOperator("$pop", -1)
  case object PopLast                    extends UpdateOperator("$pop", 1)
  // TODO: unpleasant to use if A is a QueryOperator
  final case class Pull[A](condition: A)      extends UpdateOperator("$pull", condition)
  final case class PullAll[A](values: Seq[A]) extends UpdateOperator("$pullAll", values)
  final case class PushAll[A](values: Seq[A]) extends UpdateOperator("$pushAll", values)
  final case class Push[A](value: A)          extends UpdateOperator("$push", value)

  final case class Modifiers[A](values: Seq[A],
                                slice: Option[Int],
                                position: Option[Int],
                                sort: Option[Either[Int, Sort]]) {
    def withSlice(sl: Int): Modifiers[A]     = copy(slice = Some(sl))
    def withPosition(pos: Int): Modifiers[A] = copy(position = Some(pos))
    def withSort(asc: Boolean): Modifiers[A] = copy(sort = Some(Left(if(asc) 1 else -1)))
    def withSort(srt: Sort): Modifiers[A]    = copy(sort = Some(Right(srt)))
  }
  implicit def modifiersEncoder[A: BsonValueEncoder]: BsonDocumentEncoder[Modifiers[A]] = {
    def addSlice(map: Map[String, BsonValue], sl: Option[Int])     = sl.fold(map)(i ⇒ map + ("$slice"     → BsonInt(i)))
    def addPosition(map: Map[String, BsonValue], pos: Option[Int]) = pos.fold(map)(p ⇒ map + ("$position" → BsonInt(p)))
    def addSort(map: Map[String, BsonValue], srt: Option[Either[Int, Sort]]) = srt.fold(map) { s ⇒
      map + ("$sort" → BsonValueEncoder[Either[Int, Sort]].encode(s))
    }

    BsonDocumentEncoder.from {
      case Modifiers(values, slice, position, sort) ⇒
        BsonDocument(
          addPosition(addSort(addSlice(Map("$each" → BsonValueEncoder[Seq[A]].encode(values)), slice), sort), position)
        )
    }
  }

  // - Field update ----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Inc(value: Int)          extends UpdateOperator("$inc", value)
  final case class Mul(value: Int)          extends UpdateOperator("$mul", value)
  final case class Set[A](value: A)         extends UpdateOperator("$set", value)
  final case class Rename(to: String)       extends UpdateOperator("$rename", to)
  case object Unset                         extends UpdateOperator("$unset", "")
  final case class Min[A](value: A)         extends UpdateOperator("$min", value)
  final case class Max[A](value: A)         extends UpdateOperator("$max", value)
  final case class SetOnInsert[A](value: A) extends UpdateOperator("$setOnInsert", value)
  final case class CurrentDate(value: Time) extends UpdateOperator("$currentDate", value)
  final case class Bitwise(value: BitOp)    extends UpdateOperator("$bit", value)
  // TODO:
  // - $isolated
  // - $ (update)
}
