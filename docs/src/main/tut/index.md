---
layout: index
---

[![Build Status](https://travis-ci.org/nrinaudo/kantan.mongodb.svg?branch=master)](https://travis-ci.org/nrinaudo/kantan.mongodb)
[![codecov](https://codecov.io/gh/nrinaudo/kantan.mongodb/branch/master/graph/badge.svg)](https://codecov.io/gh/nrinaudo/kantan.mongodb)
[![Latest version](https://index.scala-lang.org/nrinaudo/kantan.mongodb/kantan.mongodb/latest.svg)](https://index.scala-lang.org/nrinaudo/kantan.mongodb)
[![Join the chat at https://gitter.im/nrinaudo/kantan.mongodb](https://img.shields.io/badge/gitter-join%20chat-52c435.svg)](https://gitter.im/nrinaudo/kantan.mongodb)

kantan.mongodb is a library for interacting with mongodb written in the
[Scala programming language](http://www.scala-lang.org).

## Getting started

kantan.mongodb is currently available for Scala 2.11 and 2.12.

The current version is `@VERSION@`, which can be added to your project with one or more of the following line(s)
in your SBT build file:

```scala
// Core library, included automatically if any other module is imported.
libraryDependencies += "com.nrinaudo" %% "kantan.mongodb" % "@VERSION@"

// Java 8 date and time instances.
libraryDependencies += "com.nrinaudo" %% "kantan.mongodb-java8" % "@VERSION@"

// Automatic type class instances derivation.
libraryDependencies += "com.nrinaudo" %% "kantan.mongodb-generic" % "@VERSION@"

// Provides instances for joda time types.
libraryDependencies += "com.nrinaudo" %% "kantan.mongodb-joda-time" % "@VERSION@"
```

## Motivation

For all the criticisms it attracts, I find mongodb to be a very useful tool for some specific use cases, such as dataset
exploration. Unfortunately, the Scala libraries available behave in ways that I find unpleasant, mostly in two specific
areas:

* runtime type discovery - whether or not it's possible to decode a document as a given type is not known until runtime,
  which I feel is too late.
* safety - the mongodb libraries I worked with throw exceptions and make it almost impossible to make sure you've
  dealt with all possible error cases.

kantan.mongodb addresses these issues in the standard way: type classes for decoding, and errors directly encoded in
types.

Note that none of my current use cases require some of mongodb's most advanced or complex features. kantan.mongodb
attempts to support everything other, more official libraries do, but might fall short simply because I don't know or
forgot about a feature. Feel free to open a ticket and I'll do my best to add support if at all manageable.
