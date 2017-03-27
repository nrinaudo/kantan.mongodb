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

package kantan.mongodb.options

import com.mongodb.client.model.{ValidationAction, ValidationLevel, ValidationOptions}
import java.io.Serializable
import kantan.mongodb.{BsonDocument, BsonDocumentEncoder}

final case class ValidationOpts(action: ValidationOpts.Action, level: ValidationOpts.Level,
                                validator: Option[BsonDocument]) {
  def action(a: ValidationOpts.Action): ValidationOpts = copy(action = a)
  def level(l: ValidationOpts.Level): ValidationOpts = copy(level = l)
  def validator[V: BsonDocumentEncoder](v: V): ValidationOpts = copy(validator = Some(BsonDocumentEncoder[V].encode(v)))
  def clearValidator: ValidationOpts = copy(validator = None)

  private[mongodb] lazy val legacy: ValidationOptions = {
    val opts = new ValidationOptions().validationAction(action.legacy).validationLevel(level.legacy)
    validator.foreach(opts.validator)
    opts
  }
}

object ValidationOpts {
  sealed abstract class Action (private[mongodb] val legacy: ValidationAction) extends Product with Serializable

  object Action {
    case object Error extends Action(ValidationAction.ERROR)
    case object Warn extends Action(ValidationAction.WARN)
    val default: Action = Error
  }

  sealed abstract class Level (private[mongodb] val legacy: ValidationLevel) extends Product with Serializable

  object Level {
    case object Moderate extends Level(ValidationLevel.MODERATE)
    case object Off extends Level(ValidationLevel.OFF)
    case object Strict extends Level(ValidationLevel.STRICT)
    val defaut: Level = Strict
  }

  val default: ValidationOpts = ValidationOpts(Action.default, Level.defaut, None)
}
