@file:Suppress("UNCHECKED_CAST")

package karat.ast

import edu.mit.csail.sdg.ast.Sig

// A copy of 'sequniv'
// https://github.com/AlloyTools/org.alloytools.alloy/blob/master/org.alloytools.alloy.core/src/main/resources/models/util/sequniv.als

fun <A> seq(s: KSet<A>): KSet<List<A>> =
  KSet(Sig.PrimSig.SEQIDX.isSeq_arrow_lone(s.expr))

context(ModuleLoader) val SeqUniv: KModule
  get() = module("util/sequniv")!!

context(ModuleLoader) fun <A> isEmpty(seq: KSet<List<A>>): KFormula =
  (SeqUniv.predicate("isEmpty")!! as KPredicate1<List<A>>).invoke(seq)

context(ModuleLoader) fun <A> first(seq: KSet<List<A>>): KSet<A> =
  (SeqUniv.function("first")!! as KFunction1<List<A>, A>).invoke(seq)

context(ModuleLoader) fun <A> add(element: KSet<A>, seq: KSet<List<A>>): KSet<List<A>> =
  (SeqUniv.function("add")!! as KFunction2<List<A>, A, List<A>>).invoke(seq, element)

context(ModuleLoader) fun <A> rest(seq: KSet<List<A>>): KSet<List<A>> =
  (SeqUniv.function("rest")!! as KFunction1<List<A>, List<A>>).invoke(seq)

context(ModuleLoader) fun <A> setAt(seq: KSet<List<A>>, i: KSet<Int>, e: KSet<A>): KSet<List<A>> =
  (SeqUniv.function("setAt")!! as KFunction3<List<A>, Int, A, List<A>>).invoke(seq, i, e)

context(ModuleLoader) fun <A> elements(seq: KSet<List<A>>): KSet<A> =
  (SeqUniv.function("elems")!! as KFunction1<List<A>, A>).invoke(seq)