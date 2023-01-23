package karat.reflection

import karat.ast.KArg
import karat.ast.KFormula
import karat.ast.KTemporalFormulaBuilder
import kotlin.reflect.typeOf

context(ReflectedModule) inline fun <reified A: Any> KTemporalFormulaBuilder.transition(
  name: String? = null,
  noinline block: (KArg<A>) -> KFormula
): Unit = transition(nextUnique(typeOf<A>()) to set<A>(), block)

context(ReflectedModule) inline fun <reified A: Any> KTemporalFormulaBuilder.transition1(
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): Unit = transition(set<A>(), fn)

context(ReflectedModule) inline fun <reified A: Any, reified B: Any> KTemporalFormulaBuilder.transition(
  name: String? = null,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): Unit = transition(nextUnique(typeOf<A>()) to set<A>(), nextUnique(typeOf<B>()) to set<B>(), block)

context(ReflectedModule) inline fun <reified A: Any, reified B: Any> KTemporalFormulaBuilder.transition2(
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): Unit = transition(set<A>(), set<B>(), fn)