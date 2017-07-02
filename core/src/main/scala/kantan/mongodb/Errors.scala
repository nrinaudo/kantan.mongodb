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

import com.mongodb._
import kantan.codecs.error._

sealed abstract class MongoError(message: String, code: Int) extends Error(message)

abstract class MongoErrorCompanion[T <: MongoError](msg: String)(f: (String, Int) ⇒ T)
  extends ErrorCompanion[T](msg)(s ⇒ f(s, -4)) {
  override implicit val isError: IsError[T] = new IsError[T] {
    override def from(msg: String, cause: Throwable) = {
      val error = f(msg, cause match {
        case e: MongoException ⇒ e.getCode
        case _                 ⇒ -4
      })
      error.initCause(cause)
      error
    }

    override def fromMessage(msg: String) = from(msg, new Exception(msg))
    override def fromThrowable(cause: Throwable): T = from(Option(cause.getMessage).getOrElse(msg), cause)
  }
}

object MongoError {
  def apply(exception: MongoException): MongoError = exception match {
    case e: MongoExecutionTimeoutException   ⇒ ExecutionTimeout(e)
    case e: MongoGridFSException             ⇒ GridFS(e)
    case e: MongoIncompatibleDriverException ⇒ IncompatibleDriver(e)
    case e: MongoInternalException           ⇒ Internal(e)
    case e: MongoInterruptedException        ⇒ Interrupted(e)
    case e: MongoClientException             ⇒ Client(e)
    case e: MongoServerException             ⇒ Server(e)
    case e: MongoSocketException             ⇒ Socket(e)
    case _                                   ⇒ Unknown(exception)
  }

  // - Unknown errors --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Unknown(message: String, code: Int) extends MongoError(message, code)
  object Unknown extends MongoErrorCompanion("an unknown error has occurred")((s, c) ⇒ new Unknown(s, c))



  // - Decoding errors -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed case class Decode(message: String, code: Int) extends MongoError(message, -4)
  object Decode extends MongoErrorCompanion("an error occurred while decoding data")((s, c) ⇒
    new Decode(s, c)
  )



  // - Timeout errors --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class ExecutionTimeout(message: String, code: Int) extends MongoError(message, code)
  object ExecutionTimeout extends MongoErrorCompanion("a timeout error has occurred")((s, c) ⇒
    new ExecutionTimeout(s, c)
  )



  // - GridFS errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class GridFS(message: String, code: Int) extends MongoError(message, code)
  object GridFS extends MongoErrorCompanion("an unspecified GridFS error has occurred")((s, c) ⇒
    new GridFS(s, c)
  )



  // - Driver incompatibility ------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class IncompatibleDriver(message: String, code: Int) extends MongoError(message, code)
  object IncompatibleDriver extends MongoErrorCompanion("an unspecified driver error has occurred")((s, c) ⇒
    new IncompatibleDriver(s, c)
  )



  // - Internal errors -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Internal(message: String, code: Int) extends MongoError(message, code)
  object Internal extends MongoErrorCompanion("an unspecified internal error has occurred")((s, c) ⇒
    new Internal(s, c)
  )



  // - Interruption errors ---------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Interrupted(message: String, code: Int) extends MongoError(message, code)
  object Interrupted extends MongoErrorCompanion("an unspecified interruption error has occurred")((s, c) ⇒
    new Interrupted(s, c)
  )



  // - Client errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed abstract class Client(message: String, code: Int) extends MongoError(message, code)

  object Client {
    def apply(exception: MongoClientException): MongoError.Client = exception match {
      case e: MongoConfigurationException ⇒ MongoError.Client.Configuration(e)
      case e: MongoSecurityException      ⇒ MongoError.Client.Security(e)
      case e: MongoTimeoutException       ⇒ MongoError.Client.Timeout(e)
      case e: MongoWaitQueueFullException ⇒ MongoError.Client.WaitQueueFull(e)
      case _                              ⇒ MongoError.Client.Unknown(exception)
    }



    // - Unknown client errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Unknown(message: String, code: Int) extends Client(message, code)
    object Unknown extends MongoErrorCompanion("an unknown client error has occurred")((s, c) ⇒
      new MongoError.Client.Unknown(s, c)
    )



    // - Client configuration errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Configuration(message: String, code: Int) extends Client(message, code)
    object Configuration extends MongoErrorCompanion("an unspecified configuration error has occurred")((s, c) ⇒
      new MongoError.Client.Configuration(s, c)
    )


    // - Client security errors ----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Security(message: String, code: Int) extends Client(message, code)
    object Security extends MongoErrorCompanion("an unspecified security error has occurred")((s, c) ⇒
      new MongoError.Client.Security(s, c)
    )


    // - Client timeout errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Timeout(message: String, code: Int) extends Client(message, code)
    object Timeout extends MongoErrorCompanion("an unspecified timeout error has occurred")((s, c) ⇒
      new MongoError.Client.Timeout(s, c)
    )



    // - Client queue full errors --------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class WaitQueueFull(message: String, code: Int) extends Client(message, code)
    object WaitQueueFull extends MongoErrorCompanion("an unspecified queue full error has occurred")((s, c) ⇒
      new MongoError.Client.WaitQueueFull(s, c)
    )
  }



  // - Server errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed abstract class Server(message: String, code: Int) extends MongoError(message, code)

  object Server {
    def apply(exception: MongoServerException): MongoError.Server = exception match {
      case e: MongoBulkWriteException        ⇒ MongoError.Server.BulkWrite(e)
      case e: BulkWriteException             ⇒ MongoError.Server.BulkWrite(e)
      case e: MongoCommandException          ⇒ MongoError.Server.Command(e)
      case e: MongoNodeIsRecoveringException ⇒ MongoError.Server.NodeIsRecovering(e)
      case e: MongoNotPrimaryException       ⇒ MongoError.Server.NotPrimary(e)
      case e: MongoCursorNotFoundException   ⇒ MongoError.Server.CursorNotFound(e)
      case e: MongoQueryException            ⇒ MongoError.Server.Query(e)
      case e: MongoWriteConcernException     ⇒ MongoError.Server.WriteConcern(e)
      case e: WriteConcernException          ⇒ MongoError.Server.WriteConcern(e)
      case e: MongoWriteException            ⇒ MongoError.Server.Write(e)
      case _                                 ⇒ MongoError.Server.Unknown(exception)
    }



    // - Unknown server errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Unknown(message: String, code: Int) extends Server(message, code)
    object Unknown extends MongoErrorCompanion("an unknown server error has occurred")((s, c) ⇒
      new MongoError.Server.Unknown(s, c)
    )



    // - Server bulk write errors --------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class BulkWrite(message: String, code: Int) extends Server(message, code)
    object BulkWrite extends MongoErrorCompanion("an unspecified bulk write error has occurred")((s, c) ⇒
      new MongoError.Server.BulkWrite(s, c)
    )



    // - Server command write errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Command(message: String, code: Int) extends Server(message, code)
    object Command extends MongoErrorCompanion("an unspecified command error has occurred")((s, c) ⇒
      new MongoError.Server.Command(s, c)
    )



    // - Server node recovery errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class NodeIsRecovering(message: String, code: Int) extends Server(message, code)
    object NodeIsRecovering extends MongoErrorCompanion("an unspecified node recovery error has occurred")((s, c) ⇒
      new MongoError.Server.NodeIsRecovering(s, c)
    )



    // - Server "not primary" errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class NotPrimary(message: String, code: Int) extends Server(message, code)
    object NotPrimary extends MongoErrorCompanion("an unspecified primary error has occurred")((s, c) ⇒
      new MongoError.Server.NotPrimary(s, c)
    )



    // - Server query errors -------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Query(message: String, code: Int) extends Server(message, code)
    object Query extends MongoErrorCompanion("an unspecified query error has occurred")((s, c) ⇒
      new MongoError.Server.Query(s, c)
    )



    // - Cursor not found errors ---------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class CursorNotFound(message: String, code: Int) extends Server(message, code)
    object CursorNotFound extends MongoErrorCompanion("an cursor was not found")((s, c) ⇒
      new MongoError.Server.CursorNotFound(s, c)
    )



    // - Write concern errors ------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class WriteConcern(message: String, code: Int) extends Server(message, code)
    object WriteConcern extends MongoErrorCompanion("an unspecified write concern error has occurred")((s, c) ⇒
      new MongoError.Server.WriteConcern(s, c)
    )



    // - Write errors --------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Write(message: String, code: Int) extends Server(message, code)
    object Write extends MongoErrorCompanion("an unspecified write error has occurred")((s, c) ⇒
      new MongoError.Server.Write(s, c)
    )
  }



  // - Socket errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed abstract class Socket(message: String, code: Int) extends MongoError(message, code)

  object Socket {
    def apply(exception: MongoSocketException): MongoError.Socket = exception match {
      case e: MongoSocketClosedException ⇒ MongoError.Socket.Closed(e)
      case e: MongoSocketOpenException   ⇒ MongoError.Socket.Open(e)
      case e: MongoSocketReadException   ⇒ MongoError.Socket.ReadTimeout(e)
      case e: MongoSocketWriteException  ⇒ MongoError.Socket.Write(e)
      case _                             ⇒ MongoError.Socket.Unknown(exception)
    }



    // - Unknown socket errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Unknown(message: String, code: Int) extends Socket(message, code)
    object Unknown extends MongoErrorCompanion("an unknown socket error has occurred")((s, c) ⇒
      new MongoError.Socket.Unknown(s, c)
    )


    // - Socket closed errors ------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Closed(message: String, code: Int) extends Socket(message, code)
    object Closed extends MongoErrorCompanion("an unspecified socket closed error has occurred")((s, c) ⇒
      new MongoError.Socket.Closed(s, c)
    )



    // - Socket open errors --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Open(message: String, code: Int) extends Socket(message, code)
    object Open extends MongoErrorCompanion("an unspecified socket open error has occurred")((s, c) ⇒
      new MongoError.Socket.Open(s, c)
    )


    // - Socket timeout errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class ReadTimeout(message: String, code: Int) extends Socket(message, code)
    object ReadTimeout extends MongoErrorCompanion("an unspecified socket read timeout error has occurred")((s, c) ⇒
      new MongoError.Socket.ReadTimeout(s, c)
    )



    // - Socket Write errors -------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Write(message: String, code: Int) extends Socket(message, code)
    object Write extends MongoErrorCompanion("an unspecified socket write error has occurred")((s, c) ⇒
      new MongoError.Socket.Write(s, c)
    )
  }
}
