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

import kantan.mongodb.{BsonDocument, BsonJavaScriptWithScope}
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.CodecRegistry

class JavaScriptWithScopeCodec(registry: CodecRegistry) extends Codec[BsonJavaScriptWithScope] {
  override def decode(reader: BsonReader, context: DecoderContext) =
    BsonJavaScriptWithScope(reader.readJavaScriptWithScope(), new DocumentCodec(registry).decode(reader, context).value)
  override def encode(writer: BsonWriter, value: BsonJavaScriptWithScope, context: EncoderContext) = {
    writer.writeJavaScriptWithScope(value.value)
    new DocumentCodec(registry).encode(writer, BsonDocument(value.scope), context)
  }

  override def getEncoderClass = classOf[BsonJavaScriptWithScope]
}
