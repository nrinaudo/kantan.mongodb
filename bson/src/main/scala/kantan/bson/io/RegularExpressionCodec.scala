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

package kantan.bson.io

import java.util.regex.Pattern
import kantan.bson.BsonRegularExpression
import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}

object RegularExpressionCodec extends Codec[BsonRegularExpression] {
  private val patternFlags: List[(Int, Char)] = List(
    Pattern.CANON_EQ         → 'c',
    Pattern.UNIX_LINES       → 'd',
    256                      → 'g',
    Pattern.CASE_INSENSITIVE → 'i',
    Pattern.MULTILINE        → 'm',
    Pattern.DOTALL           → 's',
    Pattern.LITERAL          → 't',
    Pattern.UNICODE_CASE     → 'u',
    Pattern.COMMENTS         → 'x'
  )

  private def optionsAsInt(flags: String): Int = flags.foldLeft(0) { case (acc, flag) ⇒
    patternFlags.find(_._2.toLower == flag).map(_._1).fold(acc)(acc | _)
  }

  private def optionsAsString(flags: Int): String =
    patternFlags.foldLeft(new StringBuilder()) { case (acc, (i, c)) ⇒
      if((flags & i) > 0) acc.append(c)
      else                acc
    }.result()


  override def decode(reader: BsonReader, d: DecoderContext) = {
    val r = reader.readRegularExpression()
    BsonRegularExpression(Pattern.compile(r.getPattern, optionsAsInt(r.getOptions)))
  }

  override def encode(writer: BsonWriter, value: BsonRegularExpression, e: EncoderContext) =
    writer.writeRegularExpression(new org.bson.BsonRegularExpression(value.value.pattern(),
      optionsAsString(value.value.flags())))


  override def getEncoderClass = classOf[BsonRegularExpression]
}
