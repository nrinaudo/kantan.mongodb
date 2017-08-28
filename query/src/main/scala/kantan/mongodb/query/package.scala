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

import java.util.regex.Pattern
import kantan.mongodb.query.Query.{Field ⇒ QField, _}
import kantan.mongodb.query.QueryOperator._
import kantan.mongodb.query.Update.{Field ⇒ UField}
import kantan.mongodb.query.UpdateOperator._

package object query {
  // - Sort ------------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def $asc(field: String): Sort           = Sort.Ascending(field)
  def $desc(field: String): Sort          = Sort.Descending(field)
  def $metaTextScore(field: String): Sort = Sort.MetaTextScore(field)

  // - Queries ---------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def $eq[A](field: String, value: A): QField[Eq[A]]                        = QField(field, Eq(value))
  def $ne[A](field: String, value: A): Not[QField[Eq[A]]]                   = Not(QField(field, Eq(value)))
  def $gt[A](field: String, value: A): QField[Gt[A]]                        = QField(field, Gt(value))
  def $gte[A](field: String, value: A): QField[Gte[A]]                      = QField(field, Gte(value))
  def $lt[A](field: String, value: A): QField[Lt[A]]                        = QField(field, Lt(value))
  def $lte[A](field: String, value: A): QField[Lte[A]]                      = QField(field, Lte(value))
  def $in[A](field: String, values: A*): QField[In[A]]                      = QField(field, In(values))
  def $nin[A](field: String, values: A*): Not[QField[In[A]]]                = Not(QField(field, In(values)))
  def $elemMatch[A](field: String, value: A): QField[ElemMatch[A]]          = QField(field, ElemMatch(value))
  def $exists(field: String, value: Boolean): QField[Exists]                = QField(field, Exists(value))
  def $regex(field: String, value: Pattern): QField[Regex]                  = QField(field, Regex(value))
  def $size(field: String, value: Int): QField[Size]                        = QField(field, Size(value))
  def $where(field: String, value: String): QField[Where]                   = QField(field, Where(value))
  def $mod(field: String, div: Long, rem: Long): QField[Mod]                = QField(field, Mod(Mod.Value(div, rem)))
  def $all[A](field: String, values: A*): QField[All[A]]                    = QField(field, All(values))
  def $bitsAllClear(field: String, mask: Long): QField[Bits.AllClear]       = QField(field, Bits.AllClear(mask))
  def $bitsAllSet(field: String, mask: Long): QField[Bits.AllSet]           = QField(field, Bits.AllSet(mask))
  def $bitsAnyClear(field: String, mask: Long): QField[Bits.AnyClear]       = QField(field, Bits.AnyClear(mask))
  def $bitsAnySet(field: String, mask: Long): QField[Bits.AnySet]           = QField(field, Bits.AnySet(mask))
  def $geoIntersects[A](field: String, value: A): QField[Geo.Intersects[A]] = QField(field, Geo.Intersects(value))
  def $geoWithin[A](field: String, value: A): QField[Geo.Within[A]]         = QField(field, Geo.Within(value))

  // - Update ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def $each[A](values: A*): Modifiers[A] = Modifiers(values, None, None, None)

  def $addToSet[A](field: String, value: A): UField[A]       = UField(field, AddToSet(value))
  def $bit(field: String, value: BitOp): UField[BitOp]       = UField(field, Bitwise(value))
  def $currentDate(field: String, value: Time): UField[Time] = UField(field, CurrentDate(value))
  def $inc(field: String, by: Int): UField[Int]              = UField(field, Inc(by))
  def $max[A](field: String, value: A): UField[A]            = UField(field, Max(value))
  def $min[A](field: String, value: A): UField[A]            = UField(field, Min(value))
  def $mul(field: String, by: Int): UField[Int]              = UField(field, Mul(by))
  def $popFirst(field: String): UField[Int]                  = UField(field, PopFirst)
  def $popLast(field: String): UField[Int]                   = UField(field, PopLast)
  def $pull[A](field: String, condition: A): UField[A]       = UField(field, Pull(condition))
  def $pullAll[A](field: String, values: A*): UField[Seq[A]] = UField(field, PullAll(values))
  def $push[A](field: String, value: A): UField[A]           = UField(field, Push(value))
  def $pushAll[A](field: String, values: A*): UField[Seq[A]] = UField(field, PushAll(values))
  def $rename(field: String, to: String): UField[String]     = UField(field, Rename(to))
  def $set[A](field: String, value: A): UField[A]            = UField(field, Set(value))
  def $setOnInsert[A](field: String, value: A): UField[A]    = UField(field, SetOnInsert(value))
  def $unset(field: String): UField[String]                  = UField(field, Unset)
}
