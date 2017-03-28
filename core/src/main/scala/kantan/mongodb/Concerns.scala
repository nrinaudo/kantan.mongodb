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

import com.mongodb.{ReadConcern ⇒ RConcern, WriteConcern ⇒ WConcern}

sealed abstract class ReadConcern(private[mongodb] val legacy: RConcern) extends Product with Serializable
object ReadConcern {
  private[mongodb] def fromLegacy(legacy: RConcern): ReadConcern =
    values.find(_.legacy == legacy).getOrElse(sys.error(s"Unknown read concern: $legacy"))

  case object Default extends ReadConcern(RConcern.DEFAULT)
  case object Linearizable extends ReadConcern(RConcern.LINEARIZABLE)
  case object Local extends ReadConcern(RConcern.LOCAL)
  case object Majority extends ReadConcern(RConcern.MAJORITY)

  def values: List[ReadConcern] = List(Default, Linearizable, Local, Majority)
}

sealed abstract class WriteConcern(private[mongodb] val legacy: WConcern) extends Product with Serializable
object WriteConcern {
  private[mongodb] def fromLegacy(legacy: WConcern): WriteConcern =
    values.find(_.legacy == legacy).getOrElse(sys.error(s"Unknown write concern: $legacy"))

  case object Acknowledged extends WriteConcern(WConcern.ACKNOWLEDGED)
  case object Journaled extends WriteConcern(WConcern.JOURNALED)
  case object Majority extends WriteConcern(WConcern.MAJORITY)
  case object Unacknowledged extends WriteConcern(WConcern.UNACKNOWLEDGED)
  case object W1 extends WriteConcern(WConcern.W1)
  case object W2 extends WriteConcern(WConcern.W2)
  case object W3 extends WriteConcern(WConcern.W3)

  def values: List[WriteConcern] = List(Acknowledged, Journaled, Majority, Unacknowledged, W1, W2, W3)
}