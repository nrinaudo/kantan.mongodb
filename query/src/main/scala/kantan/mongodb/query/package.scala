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
import kantan.mongodb.query.Query._
import kantan.mongodb.query.QueryOperator._

package object query {
  // - Sort ------------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def $asc(field: String): Sort = Sort.Ascending(field)
  def $desc(field: String): Sort = Sort.Descending(field)
  def $metaTextScore(field: String): Sort = Sort.MetaTextScore(field)



  // - Queries ---------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def $eq[A](field: String, value: A): Field[Eq[A]]                        = Field(field, Eq(value))
  def $ne[A](field: String, value: A): Not[Field[Eq[A]]]                   = Not(Field(field, Eq(value)))
  def $gt[A](field: String, value: A): Field[Gt[A]]                        = Field(field, Gt(value))
  def $gte[A](field: String, value: A): Field[Gte[A]]                      = Field(field, Gte(value))
  def $lt[A](field: String, value: A): Field[Lt[A]]                        = Field(field, Lt(value))
  def $lte[A](field: String, value: A): Field[Lte[A]]                      = Field(field, Lte(value))
  def $in[A](field: String, values: A*): Field[In[A]]                      = Field(field, In(values))
  def $nin[A](field: String, values: A*): Not[Field[In[A]]]                = Not(Field(field, In(values)))
  def $elemMatch[A](field: String, value: A): Field[ElemMatch[A]]          = Field(field, ElemMatch(value))
  def $exists(field: String, value: Boolean): Field[Exists]                = Field(field, Exists(value))
  def $regex(field: String, value: Pattern): Field[Regex]                  = Field(field, Regex(value))
  def $size(field: String, value: Int): Field[Size]                        = Field(field, Size(value))
  def $where(field: String, value: String): Field[Where]                   = Field(field, Where(value))
  def $mod(field: String, div: Long, rem: Long): Field[Mod]                = Field(field, Mod(Mod.Value(div, rem)))
  def $all[A](field: String, values: A*): Field[All[A]]                    = Field(field, All(values))
  def $bitsAllClear(field: String, mask: Long): Field[Bits.AllClear]       = Field(field, Bits.AllClear(mask))
  def $bitsAllSet(field: String, mask: Long): Field[Bits.AllSet]           = Field(field, Bits.AllSet(mask))
  def $bitsAnyClear(field: String, mask: Long): Field[Bits.AnyClear]       = Field(field, Bits.AnyClear(mask))
  def $bitsAnySet(field: String, mask: Long): Field[Bits.AnySet]           = Field(field, Bits.AnySet(mask))
  def $geoIntersects[A](field: String, value: A): Field[Geo.Intersects[A]] = Field(field, Geo.Intersects(value))
  def $geoWithin[A](field: String, value: A): Field[Geo.Within[A]]         = Field(field, Geo.Within(value))
}
