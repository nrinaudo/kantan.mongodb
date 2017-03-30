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

import kantan.mongodb.{BsonArray, BsonDocument, BsonDouble, BsonValueEncoder}

sealed abstract class Shape extends Product with Serializable
object Shape {

  final case class Box(lowerLeftX: Double, lowerLeftY: Double, upperRightX: Double, upperRightY: Double) extends Shape

  final case class Center(x: Double, y: Double, radius: Double) extends Shape

  final case class CenterSphere(x: Double, y: Double, radius: Double) extends Shape

  final case class Polygon(points: List[(Double, Double)]) extends Shape

  implicit val shapeValueEncoder: BsonValueEncoder[Shape] = BsonValueEncoder.from {
    case Box(lx, ly, rx, ry) ⇒
      BsonDocument(Map("$box" → BsonArray(List(BsonArray(List(BsonDouble(lx), BsonDouble(ly))),
        BsonArray(List(BsonDouble(rx), BsonDouble(ry)))))))
    case Center(x, y, r) ⇒ BsonDocument(Map("$center" → BsonArray(List(
      BsonArray(List(BsonDouble(x), BsonDouble(y))),
      BsonDouble(r)
    ))))
    case CenterSphere(x, y, r) ⇒ BsonDocument(Map("$centerSphere" → BsonArray(List(
      BsonArray(List(BsonDouble(x), BsonDouble(y))),
      BsonDouble(r)
    ))))
    case Polygon(points) ⇒ BsonDocument(Map("$polygon" → BsonArray(
      points.map { case (x, y) ⇒ BsonArray(List(BsonDouble(x), BsonDouble(y))) }
    )))
  }
}
