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

package kantan.mongodb.io

import java.nio.ByteBuffer
import kantan.mongodb._
import kantan.mongodb.laws.discipline.arbitrary._
import org.bson.{BsonBinaryReader, BsonBinaryWriter}
import org.bson.codecs.{DecoderContext, EncoderContext}
import org.bson.io.BasicOutputBuffer
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class CodecTests extends FunSuite with GeneratorDrivenPropertyChecks {
  def roundTrip(doc: BsonDocument): BsonValue = {
    val out = new BasicOutputBuffer()
    io.bsonValueCodec.encode(new BsonBinaryWriter(out), doc, EncoderContext.builder.build)
    io.bsonValueCodec.decode(new BsonBinaryReader(ByteBuffer.wrap(out.getInternalBuffer)), DecoderContext.builder.build)
  }

  test("Encoding and decoding BSON documents should leave them unchanged") {
    forAll { doc: BsonDocument ⇒
      assert(doc == roundTrip(doc))
    }
  }
}
