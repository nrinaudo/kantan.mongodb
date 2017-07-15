---
layout: tutorial
title: "Basics"
section: tutorial
sort_order: 1
---

The purpose of this tutorial is to get you started with the most common kantan.mongodb use cases. Further tutorials will
tackle more advanced use cases, or take specific bits of this one and go into more details.

## Example type

### Type declaration

In this tutorial, we'll be reading and writing values of the following type:

```scala
import java.util.UUID

case class User(id: java.util.UUID, name: String, age: Int)
```

Not exactly the most exciting type in the world, but enough for us to go through some simple mongo operations.

### MongoDB integration

In order to be able to use our `User` type with kantan.mongodb, we need to provide implicit instances of
[`BsonDocumentDecoder`] (to be able to read values from mongo) and [`BsonDocumentEncoder`] (to be able to write
values to mongo).

This is more conveniently achieved through a [`BsonDocumentCodec`], which wraps both:

```scala
import kantan.mongodb._

implicit val userCodec: BsonDocumentCodec[User] =
  BsonDocumentCodec.caseCodec("_id", "name", "age")(User.apply _)(User.unapply _)
```

Let's not worry too much about the details of this yet, we'll study _encoders_ and _decoders_ in more depths later.

## Accessing a collection

In order to start working with mongo, the first thing to do is to create a client:

```scala
val client = MongoClient.local()
// client: kantan.mongodb.MongoClient = MongoClient(127.0.0.1:27017)
```

This connects to the local mongo instance, [`MongoClient`] has more options if you need them.

Once you have a client, you can get an instance of [`MongoDatabase`] through the [`database`] method,
then an instance of [`MongoCollection`] through the [`collection`] method:

```scala
val col: MongoCollection[User] = client.database("kantan_mongodb").collection("users")
// col: kantan.mongodb.MongoCollection[User] = MongoCollection(kantan_mongodb.users)
```

Note how our [`MongoCollection`] has a type parameter, `User`, which represents the kind of values it'll know to work with.

Let's make sure it's clear before we start inserting documents:

```scala
col.deleteAll()
// res2: kantan.mongodb.MongoResult[kantan.mongodb.DeleteResult] = Success(Acknowledged(2))
```

## Working with documents

### Document insertion

Now that we have an empty collection, let's create a few documents. We can simply call one of the various _insert_ methods,
such as [`insertMany`], with parameters of type `User`:

```scala
col.insertMany(
  User(UUID.randomUUID(), "Peter", 15),
  User(UUID.randomUUID(), "Bruce", 25),
  User(UUID.randomUUID(), "Tony", 21)
)
// res3: kantan.mongodb.MongoResult[Unit] = Success(())
```


### Document lookup

Now that our collection contains data, we can try and locate documents. kantan.mongodb provides convenient helpers for
collection querying in [`kantan.mongodb.query`]({{ site.baseurl }}/api/kantan/mongodb/query/index.html):

```scala
import kantan.mongodb.query._
```

This defines various query operators, such as `$eq`:


```scala
col.find($eq("name", "Peter")).foreach(println _)
// Success(User(5d65cfdc-fa79-41a0-95af-638f4ba57272,Peter,15))
```


### Document update

We can also easily update documents through one of the various _update_ methods. For example, [`updateOne`]:

```scala
col.updateOne($eq("name", "Tony"), $set("name", "Riri"))
// res5: kantan.mongodb.MongoResult[kantan.mongodb.UpdateResult] = Success(Update(1,Some(1)))

col.find().foreach(println _)
// Success(User(5d65cfdc-fa79-41a0-95af-638f4ba57272,Peter,15))
// Success(User(dc07b381-2b16-4fd4-8034-d37f575ef4e4,Bruce,25))
// Success(User(4d95bb5b-4448-4e34-ba19-e196ffe91044,Riri,21))
```


### Document deletion

Finally, deletion is done through one of the various _delete_ methods. For example, [`deleteOne`]:

```scala
col.deleteOne($eq("name", "Riri"))
// res7: kantan.mongodb.MongoResult[kantan.mongodb.DeleteResult] = Success(Acknowledged(1))
```

And we can verify that `Riri` is not in our collection anymore:

```scala
col.find().foreach(println _)
// Success(User(5d65cfdc-fa79-41a0-95af-638f4ba57272,Peter,15))
// Success(User(dc07b381-2b16-4fd4-8034-d37f575ef4e4,Bruce,25))
```

## Cleanup

When done with an instance of [`MongoClient`], it's import to [`close`] it in order to free resources:

```scala
client.close()
```

[`BsonDocumentEncoder`]:{{ site.baseurl }}/api/kantan/mongodb/index.html#BsonDocumentEncoder[A]=kantan.codecs.Encoder[kantan.mongodb.BsonDocument,A,kantan.mongodb.codecs.type]
[`BsonDocumentDecoder`]:{{ site.baseurl }}/api/kantan/mongodb/index.html#BsonDocumentDecoder[A]=kantan.codecs.Decoder[kantan.mongodb.BsonDocument,A,kantan.mongodb.MongoError.Decode,kantan.mongodb.codecs.type]
[`BsonDocumentCodec`]:{{ site.baseurl }}/api/kantan/mongodb/index.html#BsonDocumentCodec[A]=kantan.codecs.Codec[kantan.mongodb.BsonDocument,A,kantan.mongodb.MongoError.Decode,kantan.mongodb.codecs.type]
[`MongoClient`]:{{ site.baseurl }}/api/kantan/mongodb/MongoClient$.html
[`MongoDatabase`]:{{ site.baseurl }}/api/kantan/mongodb/MongoDatabase.html
[`database`]:{{ site.baseurl }}/api/kantan/mongodb/MongoClient.html#database(name:String):kantan.mongodb.MongoDatabase
[`MongoCollection`]:{{ site.baseurl }}/api/kantan/mongodb/MongoCollection.html
[`collection`]:{{ site.baseurl }}/api/kantan/mongodb/MongoDatabase.html#collection[A](name:String):kantan.mongodb.MongoCollection[A]
[`insertMany`]:{{ site.baseurl }}/api/kantan/mongodb/MongoCollection.html#insertMany(documents:A*)(implicitea:kantan.mongodb.BsonDocumentEncoder[A]):kantan.mongodb.MongoResult[Unit]
[`updateOne`]:{{ site.baseurl }}/api/kantan/mongodb/MongoCollection.html#updateOne[F,U](filter:F,update:U)(implicitevidence$18:kantan.mongodb.BsonDocumentEncoder[F],implicitevidence$19:kantan.mongodb.BsonDocumentEncoder[U]):kantan.mongodb.MongoResult[kantan.mongodb.UpdateResult]
[`deleteOne`]:{{ site.baseurl }}/api/kantan/mongodb/MongoCollection.html#deleteOne[F](filter:F)(implicitevidence$31:kantan.mongodb.BsonDocumentEncoder[F]):kantan.mongodb.MongoResult[kantan.mongodb.DeleteResult]
[`close`]:{{ site.baseurl }}/api/kantan/mongodb/MongoClient.html#close():Unit
