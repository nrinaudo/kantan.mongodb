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

import org.scalatest._

class MongoTests extends Suite {
  private val client: MongoClient = MongoClient.local()
  private val db                  = client.database("kantan_mongodb")

  override def nestedSuites = scala.collection.immutable.IndexedSeq(new Foo(db))

  override def runNestedSuites(args: Args) = {
    val ret = super.runNestedSuites(args)
    client.close()
    ret
  }
}

object MongoTests {
  abstract class WithCollection[A: BsonDocumentEncoder: BsonDocumentDecoder] extends fixture.FunSuite {
    def db: MongoDatabase

    type FixtureParam = MongoCollection[A]

    override def withFixture(test: OneArgTest) = {
      val col = db.collection[A](java.util.UUID.randomUUID().toString)
      try { withFixture(test.toNoArgTest(col)) } finally {
        col.drop()
      }
    }
  }
}

case class Bar(i: Int, b: Boolean)
object Bar {
  implicit val codec: BsonDocumentCodec[Bar] = BsonDocumentCodec.caseCodec("i", "j")(Bar.apply _)(Bar.unapply _)
}

class Foo(val db: MongoDatabase)
    extends MongoTests.WithCollection[Bar] with org.scalatest.prop.GeneratorDrivenPropertyChecks {
  test("foobar") { col ⇒
    forAll { (i: Int, b: Boolean) ⇒
      col.insertOne(Bar(i, b))
      println(col.count())
    }
  }
}
