# kantan.csv

[![Build Status](https://travis-ci.org/nrinaudo/kantan.mongodb.svg?branch=master)](https://travis-ci.org/nrinaudo/kantan.mongodb)
[![codecov](https://codecov.io/gh/nrinaudo/kantan.mongodb/branch/master/graph/badge.svg)](https://codecov.io/gh/nrinaudo/kantan.mongodb)
[![Latest version](https://index.scala-lang.org/nrinaudo/kantan.mongodb/kantan.mongodb/latest.svg)](https://index.scala-lang.org/nrinaudo/kantan.mongodb)
[![Join the chat at https://gitter.im/nrinaudo/kantan.mongodb](https://img.shields.io/badge/gitter-join%20chat-52c435.svg)](https://gitter.im/nrinaudo/kantan.mongodb)

# WIP!!

The purpose of kantan.mongodb is to ease the pain of having to interact with MongoDB by at least presenting a sane API
for it. When done, I hope to remove all notion of `MongoDBObject`, `DBObject`, `BasicDBObject`.... and have standard
Scala types and case classes instead, using type classes to turn them into and from a minimal BSON AST.

So, basically, you should be able to write:

```scala
final case class User(id: Int, name: String, age: Int)

collection.insert(User(1, "Foobar", 45))
```

kantan.mongodb is distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
