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

final case class ServerAddress(host: String, port: Int) {
  private[mongodb] def legacy: com.mongodb.ServerAddress = new com.mongodb.ServerAddress(host, port)
}

object ServerAddress {
  val defaultHost: String = "127.0.0.1"
  val defaultPort: Int    = 27017

  def default: ServerAddress             = ServerAddress(defaultHost, defaultPort)
  def apply(host: String): ServerAddress = ServerAddress(host, defaultPort)
}
