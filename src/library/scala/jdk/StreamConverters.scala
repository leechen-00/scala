/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.jdk

import java.util.stream.{DoubleStream, IntStream, LongStream, Stream, StreamSupport}

import scala.collection.Stepper
import scala.collection.convert.StreamExtensions

/** This object contains methods to create Java Streams that operate on Scala collections
  * (sequentially or in parallel). For more information on Java streams, consult the documentation
  * ([[https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html]]).
  *
  * The explicit conversion methods defined here are practical when writing Java code. For Scala
  * code, it is recommended to use the extension methods defined in [[StreamConverters.Ops]].
  *
  * Note: to convert between Scala collections and classic Java collections, use
  * [[CollectionConverters]].
  *
  * The methods `asJavaSeqStream` and `asJavaParStream` convert a collection to a Java Stream:
  *
  * {{{
  *   scala> import scala.jdk.StreamConverters.Ops._
  *
  *   scala> val s = (1 to 10).toList.asJavaSeqStream
  *   s: java.util.stream.IntStream = java.util.stream.IntPipeline$Head@7b1e5e55
  *
  *   scala> s.map(_ * 2).filter(_ > 5).toScala(List)
  *   res1: List[Int] = List(6, 8, 10, 12, 14, 16, 18, 20)
  * }}}
  *
  * Note: using parallel streams in the Scala REPL causes deadlocks, see
  * [[https://github.com/scala/bug/issues/9076]]. As a workaround, use `scala -Yrepl-class-based`.
  *
  * {{{
  *   scala> def isPrime(n: Int): Boolean = !(2 +: (3 to Math.sqrt(n).toInt by 2) exists (n % _ == 0))
  *   isPrime: (n: Int)Boolean
  *
  *   scala> (10000 to 1000000).asJavaParStream.filter(isPrime).toScala(Vector)
  *   res6: scala.collection.immutable.Vector[Int] = Vector(10007, 10009, 10037, 10039, ...
  * }}}
  *
  * A Java [[Stream]] provides operations on a sequence of elements. Streams are created from
  * [[java.util.Spliterator Spliterators]], which are similar to Iterators with the additional
  * capability to partition off some of their elements. This partitioning, if supported by the
  * Spliterator, is used for parallelizing Stream operations.
  *
  * Scala collections have a method [[scala.collection.IterableOnce.stepper `stepper`]] that
  * returns a [[Stepper]] for the collection, which in turn can be converted to a Spliterator for
  * creating a Java Stream.
  *
  * The `asJavaSeqStream ` extension method is available on any Scala collection. The
  * `asJavaParStream` extension method can only be invoked on collections where the return type of
  * the [[scala.collection.IterableOnce.stepper `stepper`]] method is marked with the
  * [[scala.collection.Stepper.EfficientSplit]] marker trait. This trait is added to steppers that
  * support partitioning, and therefore efficient parallel processing.
  *
  * The following extension methods are available:
  *
  * | Collection Type | Extension Methods |
  * | --- | --- |
  * | `IterableOnce` | `asJavaSeqStream` |
  * | `IndexedSeq`, Arrays, `BitSet`, `Accumulator`, `HashMap`, `HashSet`, `Range`, `TreeMap`, `TreeSet`, `Vector`, Strings | `asJavaParStream` |
  * | `Map` | `asJavaSeqKeyStream`, `asJavaSeqValueStream` |
  * | `HashMap`, `TreeMap` | `asJavaParKeyStream`, `asJavaParValueStream` |
  * | `Stepper` | `asJavaSeqStream` |
  * | `Stepper with EfficientSplit` | `asJavaParStream` |
  * | Strings | `asJavaSeqStream`, `asJavaParStream`, `asJavaSeqCharStream`, `asJavaParCharStream`, `asJavaSeqCodePointStream`, `asJavaParCodePointStream` |
  * | Java streams | `toScala`, `asJavaPrimitiveStream` |
  *
  * The `asJavaPrimitiveStream` method converts a `Stream[Int]` to an `IntStream`. It is the dual
  * of the `boxed` method defined on primitive streams (e.g., `IntStream.boxed` is a
  * `Stream[Integer]`).
  *
  * The `toScala` extension methods on Java streams collects the result of a stream pipeline into a
  * Scala collection, for example `stream.toScala(List)`, `stream.toScala(Vector)`. Note that
  * transformation operations on streams are lazy (also called "intermediate"), terminal operations
  * such as `forEach`, `count` or `toScala` trigger the evaluation.
  *
  * Collecting a parallel stream to a collection can be performed in parallel. This is beneficial if
  * the target collection supports efficient merging of the segments that are built in parallel.
  * To support this use case, the Scala standard library provides the [[Accumulator]] collection.
  * This collection supports efficient parallel construction, and it has specialized subtypes for
  * `Int`, `Long` and `Double` so that primitive Java streams can be collected to a Scala collection
  * without boxing the elements.
  *
  * @define parNote Note: parallel processing is only efficient for collections that have a
  *                 [[Stepper]] implementation which supports efficient splitting. For collections
  *                 where this is the case, the [[scala.collection.IterableOnce.stepper `stepper`]]
  *                 method has a return type marked `with EfficientSplit`.
  */
object StreamConverters {

  /** This object provides extension methods to create [[java.util.stream.Stream Java Streams]] that
    * operate on Scala collections (sequentially or in parallel), see [[StreamConverters]].
    */
  object Ops extends StreamExtensions

  /////////////////////////////////////
  // sequential streams for collections
  /////////////////////////////////////

  /** Create a sequential [[java.util.stream.Stream Java Stream]] for a Scala collection. */
  def asJavaSeqStream[A](cc: IterableOnce[A]): Stream[A] = StreamSupport.stream(cc.stepper.spliterator, false)

  /** Create a sequential [[java.util.stream.IntStream Java IntStream]] for a Scala collection. */
  def asJavaSeqIntStream         (cc: IterableOnce[Int]):   IntStream = StreamSupport.intStream(cc.stepper.spliterator, false)
  /** Create a sequential [[java.util.stream.IntStream Java IntStream]] for a Scala collection. */
  def asJavaSeqIntStreamFromByte (cc: IterableOnce[Byte]):  IntStream = StreamSupport.intStream(cc.stepper.spliterator, false)
  /** Create a sequential [[java.util.stream.IntStream Java IntStream]] for a Scala collection. */
  def asJavaSeqIntStreamFromShort(cc: IterableOnce[Short]): IntStream = StreamSupport.intStream(cc.stepper.spliterator, false)
  /** Create a sequential [[java.util.stream.IntStream Java IntStream]] for a Scala collection. */
  def asJavaSeqIntStreamFromChar (cc: IterableOnce[Char]):  IntStream = StreamSupport.intStream(cc.stepper.spliterator, false)

  /** Create a sequential [[java.util.stream.DoubleStream Java DoubleStream]] for a Scala collection. */
  def asJavaSeqDoubleStream         (cc: IterableOnce[Double]): DoubleStream = StreamSupport.doubleStream(cc.stepper.spliterator, false)
  /** Create a sequential [[java.util.stream.DoubleStream Java DoubleStream]] for a Scala collection. */
  def asJavaSeqDoubleStreamFromFloat(cc: IterableOnce[Float]):  DoubleStream = StreamSupport.doubleStream(cc.stepper.spliterator, false)

  /** Create a sequential [[java.util.stream.LongStream Java LongStream]] for a Scala collection. */
  def asJavaSeqLongStream(cc: IterableOnce[Long]): LongStream = StreamSupport.longStream(cc.stepper.spliterator, false)

  ///////////////////////////////////
  // parallel streams for collections
  ///////////////////////////////////

  /** Create a parallel [[java.util.stream.Stream Java Stream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParStream[A](cc: IterableOnce[A]): Stream[A] = StreamSupport.stream(cc.stepper.spliterator, true)

  /** Create a parallel [[java.util.stream.IntStream Java IntStream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParIntStream         (cc: IterableOnce[Int]):   IntStream = StreamSupport.intStream(cc.stepper.spliterator, true)
  /** Create a parallel [[java.util.stream.IntStream Java IntStream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParIntStreamFromByte (cc: IterableOnce[Byte]):  IntStream = StreamSupport.intStream(cc.stepper.spliterator, true)
  /** Create a parallel [[java.util.stream.IntStream Java IntStream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParIntStreamFromShort(cc: IterableOnce[Short]): IntStream = StreamSupport.intStream(cc.stepper.spliterator, true)
  /** Create a parallel [[java.util.stream.IntStream Java IntStream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParIntStreamFromChar (cc: IterableOnce[Char]):  IntStream = StreamSupport.intStream(cc.stepper.spliterator, true)

  /** Create a parallel [[java.util.stream.DoubleStream Java DoubleStream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParDoubleStream         (cc: IterableOnce[Double]): DoubleStream = StreamSupport.doubleStream(cc.stepper.spliterator, true)
  /** Create a parallel [[java.util.stream.DoubleStream Java DoubleStream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParDoubleStreamFromFloat(cc: IterableOnce[Float]):  DoubleStream = StreamSupport.doubleStream(cc.stepper.spliterator, true)

  /** Create a parallel [[java.util.stream.LongStream Java LongStream]] for a Scala collection.
    *
    * $parNote
    */
  def asJavaParLongStream(cc: IterableOnce[Long]): LongStream = StreamSupport.longStream(cc.stepper.spliterator, true)
}
