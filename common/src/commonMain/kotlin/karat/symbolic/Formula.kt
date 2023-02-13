package karat.symbolic

import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

public interface Formula
public interface Expr<out A>
public typealias Relation<A, B> = Expr<Pair<A, B>>

// basic forms of sets and relations

public data class TypeSet<out A>(
  val type: KType
): Expr<A>
public data class FieldRelation<A, out B>(
  val type: KType,
  val property: KProperty1<A, B>
): Relation<A, B>
public data class GlobalField<out A>(
  val property: KProperty0<A>
): Expr<A>

// logic operators

public object TRUE: Formula
public object FALSE: Formula

public data class Not(val formula: Formula): Formula
public data class And(val formulae: List<Formula>): Formula
public data class Or(val formula: List<Formula>): Formula
public data class Implies(val condition: Formula, val then: Formula): Formula
public data class Iff(val condition: Formula, val then: Formula): Formula
public data class IfThenElse(val condition: Formula, val then: Formula, val orElse: Formula): Formula

public enum class Quantifier(public val alloyName: String) {
  ALL("all"),
  NO("no"),
  OPTIONAL("lone"),
  SINGLE("one"),
  EXISTS("some")
}

public data class Quantified<A>(
  val quantifier: Quantifier,
  val over: Expr<A>,
  val formula: (Expr<A>) -> Formula
): Formula

public data class SuchThat<A>(
  val over: Expr<A>,
  val formula: (Expr<A>) -> Formula
): Expr<A>

// temporal operators

public data class Next<A>(val x: Expr<A>): Expr<A>
public data class Always(val formula: Formula): Formula
public data class Eventually(val formula: Formula): Formula
public data class Historically(val formula: Formula): Formula
public data class After(val formula: Formula): Formula
public data class Before(val formula: Formula): Formula
public data class Once(val formula: Formula): Formula
public data class Until(val condition: Formula, val then: Formula): Formula
public data class Releases(val condition: Formula, val then: Formula): Formula
public data class Since(val condition: Formula, val then: Formula): Formula
public data class Triggered(val condition: Formula, val then: Formula): Formula

// set operators

public data class Cardinality<A>(val x: Expr<A>): Expr<Int>
public data class Union<A>(val x: Expr<A>, val y: Expr<A>): Expr<A>
public data class Override<A>(val x: Expr<A>, val y: Expr<A>): Expr<A>
public data class Minus<A>(val x: Expr<A>, val y: Expr<A>): Expr<A>
public data class Intersect<A>(val x: Expr<A>, val y: Expr<A>): Expr<A>
public data class Product<A, B>(val x: Expr<A>, val y: Expr<B>): Expr<Pair<A, B>>
public data class Equals<A>(val x: Expr<A>, val y: Expr<A>): Formula

// relation operators

public data class Transpose<A, B>(val r: Relation<B, A>): Relation<A, B>
public data class Closure<A>(val r: Relation<A, A>): Relation<A, A>
public data class ReflexiveClosure<A>(val r: Relation<A, A>): Relation<A, A>
public data class Domain<A>(val r: Relation<A, *>): Expr<A>
public data class Range<B>(val r: Relation<*, B>): Expr<B>

// join operators

public data class JoinRelRel<A, B, C>(val r1: Relation<A, B>, val r2: Relation<B, C>): Relation<A, C>
public data class JoinSetRel<A, B>(val s: Expr<A>, val r: Relation<A, B>): Expr<B>
public data class JoinRelSet<A, B>(val r: Relation<A, B>, val s: Expr<B>): Expr<A>

// flatMap-like operators

public interface Flattener<R, A> {
  public class Id<A>: Flattener<A, A>
  public data class Pair<A, R, B>(val range: Flattener<R, B>): Flattener<kotlin.Pair<A, R>, kotlin.Pair<A, B>>
  public class Set<A>: Flattener<kotlin.collections.Set<A>, A>
  public class Nullable<A>: Flattener<A?, A>
  public class Map<A, B>: Flattener<kotlin.collections.Map<A, B>, kotlin.Pair<A, B>>
}
public data class Flatten<R, A>(val x: Expr<R>, val f: Flattener<R, A>): Expr<A>

// list operators

public data class ListIsEmpty<A>(val x: Expr<List<A>>): Formula
public data class ListFirst<A>(val x: Expr<List<A>>): Expr<A>
public data class ListAdd<A>(val x: Expr<List<A>>, val y: Expr<A>): Expr<List<A>>
public data class ListRest<A>(val x: Expr<List<A>>): Expr<List<A>>
public data class ListElements<A>(val x: Expr<List<A>>): Expr<A>

// integer operators

public data class Number(val n: Int): Expr<Int>
public enum class NumberRelation { GT, GTE, LT, LTE }
public data class NumberComparison(val r: NumberRelation, val x: Expr<Int>, val y: Expr<Int>): Formula