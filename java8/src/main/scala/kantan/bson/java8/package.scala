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

package kantan.bson

import java.time._

package object java8 {
  implicit val java8InstantDecoder: BsonValueDecoder[Instant] = BsonValueDecoder.fromSafe {
    case BsonDateTime(i) ⇒ Instant.ofEpochMilli(i)
  }
  implicit val java8ZonedDateTimeDecoder: BsonValueDecoder[ZonedDateTime] =
    java8InstantDecoder.map(i ⇒ ZonedDateTime.ofInstant(i, ZoneId.of("UTC")))
  implicit val java8OffsetDateTimeDecoder: BsonValueDecoder[OffsetDateTime] =
    java8InstantDecoder.map(i ⇒ OffsetDateTime.ofInstant(i, ZoneId.of("UTC")))

  implicit val java8InstantEncoder: BsonValueEncoder[Instant] = BsonValueEncoder.from(i ⇒ BsonDateTime(i.toEpochMilli))
  implicit val java8ZonedDateTimeEncoder: BsonValueEncoder[ZonedDateTime] = java8InstantEncoder.contramap(_.toInstant)
  implicit val java8OffsetDateTimeEncoder: BsonValueEncoder[OffsetDateTime] = java8InstantEncoder.contramap(_.toInstant)
}
