package karat.examples.gh_alerts

import karat.*
import karat.ast.*
import karat.reflection.*

data class Topic<out Message>(val partitions: Set<Partition<Message>>)

context(ReflectedModule) inline fun <reified M> singleEmptyPartition(t: KSet<Topic<M>>): KFormula = and {
  + empty(t)
  + one (t.partitions)
}

context(ReflectedModule) inline val <reified M> KSet<Topic<M>>.partitions: KSet<Partition<M>>
  get() = (this / Topic<M>::partitions).flatten

data class Partition<out Message>(var messages: List<@UnsafeVariance Message>)

context(ReflectedModule) inline val <reified M> KSet<Partition<M>>.messages: KSet<List<M>>
  get() = this / Partition<M>::messages

context(ReflectedModule) inline fun <reified M> empty(t: KSet<Topic<M>>): KFormula =
  forAll("p" to t.partitions) { p ->
    isEmpty(p / Partition<M>::messages)
  }

context(ReflectedModule) inline fun <reified M> unchangedTopic(t: KSet<Topic<M>>): KFormula =
  forAll("p" to t.partitions) { unchangedPartitions(it) }

context(ReflectedModule) inline fun <reified M> unchangedPartitions(ps: KSet<Partition<M>>): KFormula =
  forAll("p" to ps) { p: KArg<Partition<M>> ->
    stays(p.messages)
  }

context(ReflectedModule) inline fun <reified M> KSet<Topic<M>>.send(m: KArg<M>): KFormula =
  forOne("p" to partitions) { p -> and {
    + (next(p.messages) `==` add(m, current(p.messages)))
    + unchangedPartitions(partitions - p)
  } }

context(ReflectedModule) inline fun <reified M> KSet<Topic<M>>.consume(m: KArg<M>): KFormula =
  forOne("p" to partitions) { p -> and {
    + (m `==` first(p.messages))
    + (next(p.messages) `==` rest(current(p.messages)))
    + unchangedPartitions(partitions - p)
  } }