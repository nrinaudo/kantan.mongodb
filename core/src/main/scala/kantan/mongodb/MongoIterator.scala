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

import com.mongodb.client.MongoIterable
import kantan.codecs.Decoder
import kantan.codecs.Result.{Failure, Success}
import kantan.codecs.resource.ResourceIterator

private[mongodb] object MongoIterator {
  def apply[E, D](iterable: MongoIterable[E])(implicit decoder: Decoder[E, D, MongoError.Decode, codecs.type])
  : ResourceIterator[MongoResult[D]] = {
    MongoResult(iterable.iterator()) match {
      case f@Failure(_) ⇒ ResourceIterator(f)
      case Success(iterator) ⇒ new ResourceIterator[MongoResult[D]] {
        override protected def readNext() = for {
          doc ← MongoResult(iterator.next())
          d ← decoder.decode(doc)
        } yield d
        override protected def checkNext = iterator.hasNext
        override protected def release() = iterator.close()
      }
    }
  }
}