# kantan.mongodb

[![Build Status](https://travis-ci.org/nrinaudo/kantan.mongodb.svg?branch=master)](https://travis-ci.org/nrinaudo/kantan.mongodb)
[![codecov](https://codecov.io/gh/nrinaudo/kantan.mongodb/branch/master/graph/badge.svg)](https://codecov.io/gh/nrinaudo/kantan.mongodb)
[![Latest version](https://index.scala-lang.org/nrinaudo/kantan.mongodb/kantan.mongodb/latest.svg)](https://index.scala-lang.org/nrinaudo/kantan.mongodb)
[![Join the chat at https://gitter.im/nrinaudo/kantan.mongodb](https://img.shields.io/badge/gitter-join%20chat-52c435.svg)](https://gitter.im/nrinaudo/kantan.mongodb)

The purpose of kantan.mongodb is to ease the pain of having to interact with MongoDB by at least presenting a sane API
for it.

Documentation and tutorials are available on the [companion site](https://nrinaudo.github.io/kantan.mongodb/), but for those
looking for a few quick examples:

```scala
import java.util.UUID
import kantan.mongodb._
import kantan.mongodb.query._

// Declares a type and how to to encode to / decode from BSON
final case class User(id: UUID, name: String, age: Int)
implicit val userCodec: BsonDocumentCodec[User] =
  BsonDocumentCodec.caseCodec("_id", "name", "age")(User.apply _)(User.unapply _)

// Gets a collection of documents of type User.
val col = MongoClient.local().database("kantan_mongodb").collection[User]("users")

// Inserts a document
col.insert(User(UUID.randomUUID(), "Tony", 21))

// Finds a document
col.find($eq("name", "Tony"))

// Updates a document
col.updateOne($eq("name", "Tony"), $set("name", "Riri") && $set("age", 15))

// Deletes a document
col.deleteOne($eq("name", "Riri"))
```

kantan.mongodb is distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
