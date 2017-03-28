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

sealed abstract class Sort extends Product with Serializable {
  def asc(field: String): Sort = Sort.Compound(List(Sort.Ascending(field), this))
  def desc(field: String): Sort = Sort.Compound(List(Sort.Descending(field), this))
  def metaTextScore(field: String): Sort = Sort.Compound(List(Sort.MetaTextScore(field), this))
}

object Sort {
  def asc(field: String): Sort = Ascending(field)
  def desc(field: String): Sort = Descending(field)
  def metaTextScore(field: String): Sort = MetaTextScore(field)

  final case class Ascending(field: String) extends Sort
  final case class Descending(field: String) extends Sort
  final case class MetaTextScore(field: String) extends Sort
  final case class Compound(sorts: List[Sort]) extends Sort {
    override def asc(field: String) = copy(sorts = Ascending(field) :: sorts)
    override def desc(field: String) = copy(sorts = Descending(field) :: sorts)
    override def metaTextScore(field: String) = copy(sorts = MetaTextScore(field) :: sorts)
  }

  implicit val sortBsonDocumentEncoder: BsonDocumentEncoder[Sort] = BsonDocumentEncoder.from {
    case Ascending(field)     ⇒ BsonDocument(Map(field → BsonInt(1)))
    case Descending(field)    ⇒ BsonDocument(Map(field → BsonInt(-1)))
    case MetaTextScore(field) ⇒ BsonDocument(Map(field → BsonDocument(Map("$meta" → BsonString("textScore")))))
    case Compound(sorts)      ⇒ BsonDocument(sorts.foldLeft(Map.empty[String, BsonValue]) { (m, s) ⇒
      m ++ BsonDocumentEncoder[Sort].encode(s).value
    })
  }
}
