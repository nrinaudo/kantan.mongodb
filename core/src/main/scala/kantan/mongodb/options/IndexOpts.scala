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
package options

import IndexOpts.SpecialisedOpts
import com.mongodb.client.model.IndexOptions
import scala.concurrent.duration.Duration

final case class IndexOpts(collation: Option[Collation],
                           expiresAfter: Option[Duration],
                           name: Option[String],
                           partialFilterExpression: Option[BsonDocument],
                           storageEngine: Option[BsonDocument],
                           version: Int,
                           sparse: Boolean,
                           unique: Boolean,
                           specialised: Option[SpecialisedOpts]) {
  def collation(c: Collation): IndexOpts          = copy(collation = Some(c))
  def expiresAfter(duration: Duration): IndexOpts = copy(expiresAfter = Some(duration))
  def name(n: String): IndexOpts                  = copy(name = Some(n))
  def partialFilterExpression[P: BsonDocumentEncoder](p: P): IndexOpts =
    copy(partialFilterExpression = Some(BsonDocumentEncoder[P].encode(p)))
  def storageEngine[S: BsonDocumentEncoder](s: S): IndexOpts =
    copy(storageEngine = Some(BsonDocumentEncoder[S].encode(s)))
  def version(v: Int): IndexOpts                 = copy(version = v)
  def sparse(s: Boolean): IndexOpts              = copy(sparse = s)
  def unique(u: Boolean): IndexOpts              = copy(unique = u)
  def specialised(s: SpecialisedOpts): IndexOpts = copy(specialised = Some(s))

  private[mongodb] lazy val legacy: IndexOptions = {
    val opts = new IndexOptions().version(version).sparse(sparse).unique(unique)

    collation.foreach(c ⇒ opts.collation(c.legacy))
    storageEngine.foreach(opts.storageEngine)
    partialFilterExpression.foreach(opts.partialFilterExpression)
    expiresAfter.foreach(m ⇒ opts.expireAfter(m.length, m.unit))
    name.foreach(opts.name)

    specialised.foreach {
      case IndexOpts.Text(lang, langOverride, weights, version) ⇒
        opts.defaultLanguage(lang).textVersion(version)
        langOverride.foreach(opts.languageOverride)
        weights.foreach(opts.weights)
      case IndexOpts.GeoHaystack(bucketSize)       ⇒ opts.bucketSize(bucketSize)
      case IndexOpts.TwoDimensions(bits, min, max) ⇒ opts.bits(bits).min(min).max(max)
      case IndexOpts.TwoDimensionsSphere(version)  ⇒ opts.sphereVersion(version)
    }

    opts
  }
}

object IndexOpts {
  val default: IndexOpts = IndexOpts(None, None, None, None, None, 1, false, false, None)

  sealed abstract class SpecialisedOpts extends Product with Serializable

  final case class Text(defaultLanguage: String,
                        languageOverride: Option[String],
                        weights: Option[BsonDocument],
                        version: Int)
      extends SpecialisedOpts {
    def defaultLanguage(s: String): Text            = copy(defaultLanguage = s)
    def languageOverride(s: String): Text           = copy(languageOverride = Some(s))
    def weights[W: BsonDocumentEncoder](w: W): Text = copy(weights = Some(BsonDocumentEncoder[W].encode(w)))
    def version(v: Int): Text                       = copy(version = v)
  }
  object Text {
    val default: Text = Text("english", None, None, 2)
  }

  final case class GeoHaystack(bucketSize: Double) extends SpecialisedOpts {
    def bucketSize(d: Double): GeoHaystack = copy(bucketSize = d)
  }

  final case class TwoDimensions(bits: Int, min: Double, max: Double) extends SpecialisedOpts {
    def bits(b: Int): TwoDimensions   = copy(bits = b)
    def min(m: Double): TwoDimensions = copy(min = m)
    def max(m: Double): TwoDimensions = copy(max = m)
  }

  object TwoDimensions {
    val default: TwoDimensions = TwoDimensions(26, -180.0, 180.0)
  }

  final case class TwoDimensionsSphere(version: Int) extends SpecialisedOpts {
    def version(v: Int): TwoDimensionsSphere = copy(version = v)
  }

  object TwoDimensionsSphere {
    val default: TwoDimensionsSphere = TwoDimensionsSphere(2)
  }
}
