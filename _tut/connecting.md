---
layout: tutorial
title: "Connecting to MongoDB"
section: tutorial
sort_order: 1
---


```scala
import kantan.mongodb._
```

```scala
scala> val client = MongoClient.local()
client: kantan.mongodb.MongoClient = MongoClient(127.0.0.1:27017)
```

```scala
scala> val col = client.database("kantan_mongodb").collection("test")
col: kantan.mongodb.MongoCollection[Nothing] = MongoCollection(kantan_mongodb.test)
```

```scala
scala> col.deleteAll()
res0: kantan.mongodb.MongoResult[kantan.mongodb.DeleteResult] = Success(Acknowledged(0))
```


```scala
client.close()
```
