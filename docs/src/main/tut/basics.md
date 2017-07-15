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

```tut:silent
import java.util.UUID

case class User(id: java.util.UUID, name: String, age: Int)
```

Not exactly the most exciting type in the world, but enough for us to go through some simple mongo operations.

### MongoDB integration

In order to be able to use our `User` type with kantan.mongodb, we need to provide implicit instances of
[`BsonDocumentDecoder`] (to be able to read values from mongo) and [`BsonDocumentEncoder`] (to be able to write
values to mongo).

This is more conveniently achieved through a [`BsonDocumentCodec`], which wraps both:

```tut:silent
import kantan.mongodb._

implicit val userCodec: BsonDocumentCodec[User] =
  BsonDocumentCodec.caseCodec("_id", "name", "age")(User.apply _)(User.unapply _)
```

Let's not worry too much about the details of this yet, we'll study _encoders_ and _decoders_ in more depths later.

## Accessing a collection

In order to start working with mongo, the first thing to do is to create a client:

```tut:book
val client = MongoClient.local()
```

This connects to the local mongo instance, [`MongoClient`] has more options if you need them.

Once you have a client, you can get an instance of [`MongoDatabase`] through the [`database`] method,
then an instance of [`MongoCollection`] through the [`collection`] method:

```tut:book
val col: MongoCollection[User] = client.database("kantan_mongodb").collection("users")
```

Note how our [`MongoCollection`] has a type parameter, `User`, which represents the kind of values it'll know to work with.

Let's make sure it's clear before we start inserting documents:

```tut:book
col.deleteAll()
```

## Working with documents

### Document insertion

Now that we have an empty collection, let's create a few documents. We can simply call one of the various _insert_ methods,
such as [`insertMany`], with parameters of type `User`:

```tut:book
col.insertMany(
  User(UUID.randomUUID(), "Peter", 15),
  User(UUID.randomUUID(), "Bruce", 25),
  User(UUID.randomUUID(), "Tony", 21)
)
```


### Document lookup

Now that our collection contains data, we can try and locate documents. kantan.mongodb provides convenient helpers for
collection querying in [`kantan.mongodb.query`]({{ site.baseurl }}/api/kantan/mongodb/query/index.html):

```tut:silent
import kantan.mongodb.query._
```

This defines various query operators, such as `$eq`:


```tut:book
col.find($eq("name", "Peter")).foreach(println _)
```


### Document update

We can also easily update documents through one of the various _update_ methods. For example, [`updateOne`]:

```tut:book
col.updateOne($eq("name", "Tony"), $set("name", "Riri"))

col.find().foreach(println _)
```


### Document deletion

Finally, deletion is done through one of the various _delete_ methods. For example, [`deleteOne`]:

```tut:book
col.deleteOne($eq("name", "Riri"))
```

And we can verify that `Riri` is not in our collection anymore:

```tut:book
col.find().foreach(println _)
```

## Cleanup

When done with an instance of [`MongoClient`], it's import to [`close`] it in order to free resources:

```tut:silent
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
