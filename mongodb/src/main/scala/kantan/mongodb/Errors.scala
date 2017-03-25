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
import kantan.codecs.{Error, ErrorCompanion}

sealed abstract class MongoError(message: String) extends Error(message)

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
  final case class Unknown(message: String) extends MongoError(message)
  object Unknown extends ErrorCompanion("an unknown error has occurred")(s ⇒ new Unknown(s))



  // - Timeout errors --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class ExecutionTimeout(message: String) extends MongoError(message)
  object ExecutionTimeout extends ErrorCompanion("a timeout error has occurred")(s ⇒ new ExecutionTimeout(s))



  // - GridFS errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class GridFS(message: String) extends MongoError(message)
  object GridFS extends ErrorCompanion("an unspecified GridFS error has occurred")(s ⇒ new GridFS(s))



  // - Driver incompatibility ------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class IncompatibleDriver(message: String) extends MongoError(message)
  object IncompatibleDriver extends ErrorCompanion("an unspecified driver incompatibility error has occurred")(s ⇒
    new IncompatibleDriver(s)
  )



  // - Internal errors -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Internal(message: String) extends MongoError(message)
  object Internal extends ErrorCompanion("an unspecified internal error has occurred")(s ⇒ new Internal(s))



  // - Interruption errors ---------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  final case class Interrupted(message: String) extends MongoError(message)
  object Interrupted extends ErrorCompanion("an unspecified interruption error has occurred")(s ⇒ new Interrupted(s))



  // - Client errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed abstract class Client(message: String) extends MongoError(message)

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
    final case class Unknown(message: String) extends Client(message)
    object Unknown extends ErrorCompanion("an unknown client error has occurred")(s ⇒ new MongoError.Client.Unknown(s))



    // - Client configuration errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Configuration(message: String) extends Client(message)
    object Configuration extends ErrorCompanion("an unspecified configuration error has occurred")(s ⇒
      new MongoError.Client.Configuration(s)
    )


    // - Client security errors ----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Security(message: String) extends Client(message)
    object Security extends ErrorCompanion("an unspecified security error has occurred")(s ⇒
      new MongoError.Client.Security(s)
    )


    // - Client timeout errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Timeout(message: String) extends Client(message)
    object Timeout extends ErrorCompanion("an unspecified timeout error has occurred")(s ⇒
      new MongoError.Client.Timeout(s)
    )



    // - Client queue full errors --------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class WaitQueueFull(message: String) extends Client(message)
    object WaitQueueFull extends ErrorCompanion("an unspecified queue full error has occurred")(s ⇒
      new MongoError.Client.WaitQueueFull(s)
    )
  }



  // - Server errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed abstract class Server(message: String) extends MongoError(message)

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
    final case class Unknown(message: String) extends Server(message)
    object Unknown extends ErrorCompanion("an unknown server error has occurred")(s ⇒ new MongoError.Server.Unknown(s))



    // - Server bulk write errors --------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class BulkWrite(message: String) extends Server(message)
    object BulkWrite extends ErrorCompanion("an unspecified bulk write error has occurred")(s ⇒
      new MongoError.Server.BulkWrite(s)
    )



    // - Server command write errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Command(message: String) extends Server(message)
    object Command extends ErrorCompanion("an unspecified command error has occurred")(s ⇒
      new MongoError.Server.Command(s)
    )



    // - Server node recovery errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class NodeIsRecovering(message: String) extends Server(message)
    object NodeIsRecovering extends ErrorCompanion("an unspecified node recovery error has occurred")(s ⇒
      new MongoError.Server.NodeIsRecovering(s)
    )



    // - Server "not primary" errors -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class NotPrimary(message: String) extends Server(message)
    object NotPrimary extends ErrorCompanion("an unspecified primary error has occurred")(s ⇒
      new MongoError.Server.NotPrimary(s)
    )



    // - Server query errors -------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Query(message: String) extends Server(message)
    object Query extends ErrorCompanion("an unspecified query error has occurred")(s ⇒
      new MongoError.Server.Query(s)
    )



    // - Cursor not found errors ---------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class CursorNotFound(message: String) extends Server(message)
    object CursorNotFound extends ErrorCompanion("an cursor was not found")(s ⇒
      new MongoError.Server.CursorNotFound(s)
    )



    // - Write concern errors ------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class WriteConcern(message: String) extends Server(message)
    object WriteConcern extends ErrorCompanion("an unspecified write concern error has occurred")(s ⇒
      new MongoError.Server.WriteConcern(s)
    )



    // - Write errors --------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Write(message: String) extends Server(message)
    object Write extends ErrorCompanion("an unspecified write error has occurred")(s ⇒
      new MongoError.Server.Write(s)
    )
  }



  // - Socket errors ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  sealed abstract class Socket(message: String) extends MongoError(message)

  object Socket {
    def apply(exception: MongoSocketException): MongoError.Socket = exception match {
      case e: MongoSocketClosedException ⇒ MongoError.Socket.Closed(exception)
      case e: MongoSocketOpenException   ⇒ MongoError.Socket.Open(exception)
      case e: MongoSocketReadException   ⇒ MongoError.Socket.ReadTimeout(exception)
      case e: MongoSocketWriteException  ⇒ MongoError.Socket.Write(exception)
      case _                             ⇒ MongoError.Socket.Unknown(exception)
    }



    // - Unknown socket errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Unknown(message: String) extends Socket(message)
    object Unknown extends ErrorCompanion("an unknown socket error has occurred")(s ⇒ new MongoError.Socket.Unknown(s))


    // - Socket closed errors ------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Closed(message: String) extends Socket(message)
    object Closed extends ErrorCompanion("an unspecified socket closed error has occurred")(s ⇒
      new MongoError.Socket.Closed(s)
    )



    // - Socket open errors --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Open(message: String) extends Socket(message)
    object Open extends ErrorCompanion("an unspecified socket open error has occurred")(s ⇒
      new MongoError.Socket.Open(s)
    )


    // - Socket timeout errors -----------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class ReadTimeout(message: String) extends Socket(message)
    object ReadTimeout extends ErrorCompanion("an unspecified socket read timeout error has occurred")(s ⇒
      new MongoError.Socket.ReadTimeout(s)
    )



    // - Socket Write errors -------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    final case class Write(message: String) extends Socket(message)
    object Write extends ErrorCompanion("an unspecified socket write error has occurred")(s ⇒
      new MongoError.Socket.Write(s)
    )
  }
}
