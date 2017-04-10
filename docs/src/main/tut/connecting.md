---
layout: tutorial
title: "Connecting to MongoDB"
section: tutorial
sort_order: 1
---


```tut:silent
import kantan.mongodb._
```

```tut
val client = MongoClient.local()
```

```tut
val col = client.database("kantan_mongodb").collection("test")
```

```tut
col.deleteAll()
```


```tut:silent
client.close()
```
