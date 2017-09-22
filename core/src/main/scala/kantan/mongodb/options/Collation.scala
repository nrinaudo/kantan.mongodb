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

package kantan.mongodb.options

import com.mongodb.client.model.{CollationAlternate, CollationCaseFirst, CollationMaxVariable, CollationStrength}
import java.io.Serializable

final case class Collation(normalisation: Boolean,
                           caseLevel: Boolean,
                           numericOrdering: Boolean,
                           locale: String,
                           backwards: Boolean,
                           alternate: Collation.Alternate,
                           caseFirst: Collation.CaseFirst,
                           strength: Collation.Strength,
                           maxVariable: Collation.MaxVariable) {
  def normalisation(n: Boolean): Collation             = copy(normalisation = n)
  def caseLevel(c: Boolean): Collation                 = copy(caseLevel = c)
  def numericOrdering(n: Boolean): Collation           = copy(numericOrdering = n)
  def locale(l: String): Collation                     = copy(locale = l)
  def backwards(b: Boolean): Collation                 = copy(backwards = b)
  def alternate(a: Collation.Alternate): Collation     = copy(alternate = a)
  def caseFirst(c: Collation.CaseFirst): Collation     = copy(caseFirst = c)
  def strength(s: Collation.Strength): Collation       = copy(strength = s)
  def maxVariable(m: Collation.MaxVariable): Collation = copy(maxVariable = m)

  private[mongodb] lazy val legacy: com.mongodb.client.model.Collation = {
    val builder = com.mongodb.client.model.Collation.builder()

    builder.normalization(normalisation)
    builder.caseLevel(caseLevel)
    builder.numericOrdering(numericOrdering)
    builder.locale(locale)
    builder.backwards(backwards)
    builder.collationAlternate(alternate.legacy)
    builder.collationCaseFirst(caseFirst.legacy)
    builder.collationStrength(strength.legacy)
    builder.collationMaxVariable(maxVariable.legacy)

    builder.build()
  }
}

object Collation {
  val default: Collation = Collation(
    false,
    false,
    false,
    "simple",
    false,
    Alternate.default,
    CaseFirst.default,
    Strength.default,
    MaxVariable.default
  )

  sealed abstract class Alternate(private[mongodb] val legacy: CollationAlternate) extends Product with Serializable
  object Alternate {
    case object NonIgnorable extends Alternate(CollationAlternate.NON_IGNORABLE)
    case object Shifted      extends Alternate(CollationAlternate.SHIFTED)
    val default: Alternate = NonIgnorable
  }

  sealed abstract class CaseFirst(private[mongodb] val legacy: CollationCaseFirst) extends Product with Serializable
  object CaseFirst {
    case object Lower extends CaseFirst(CollationCaseFirst.LOWER)
    case object Off   extends CaseFirst(CollationCaseFirst.OFF)
    case object Upper extends CaseFirst(CollationCaseFirst.UPPER)
    val default: CaseFirst = Off
  }

  sealed abstract class Strength(private[mongodb] val legacy: CollationStrength) extends Product with Serializable
  object Strength {
    case object Identical  extends Strength(CollationStrength.IDENTICAL)
    case object Primary    extends Strength(CollationStrength.PRIMARY)
    case object Quaternary extends Strength(CollationStrength.QUATERNARY)
    case object Secondary  extends Strength(CollationStrength.SECONDARY)
    case object Tertiary   extends Strength(CollationStrength.TERTIARY)
    val default: Strength = Tertiary
  }

  sealed abstract class MaxVariable(private[mongodb] val legacy: CollationMaxVariable) extends Product with Serializable
  object MaxVariable {
    case object Punct extends MaxVariable(CollationMaxVariable.PUNCT)
    case object Space extends MaxVariable(CollationMaxVariable.SPACE)
    val default: MaxVariable = Space
  }
}
