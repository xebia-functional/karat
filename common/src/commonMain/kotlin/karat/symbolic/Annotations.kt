package karat.symbolic

import kotlin.reflect.KClass

/**
 * This property should be reflected in symbolic execution
 * (for example, when using Alloy).
 */
@Target(AnnotationTarget.PROPERTY)
public annotation class reflect

/**
 * There are no instances of this type
 * other than those of their descendants.
 */
@Target(AnnotationTarget.CLASS)
public annotation class abstract

/**
 * This is a mere subset of another class,
 * it doesn't participate in closedness.
 */
@Target(AnnotationTarget.CLASS)
public annotation class subset

/**
 * The class or field varies in time for symbolic execution purposes.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
public annotation class variable

/**
 * To be applied to a companion object.
 */
public interface Fact

/**
 * To be applied to a companion object.
 */
public interface InstanceFact<A>: Fact {
  /**
   * Represents the object to which the fact is applied to.
   */
  public val self: Expr<A>
}

/**
 * To be applied to a companion object.
 */
public sealed interface Transition
public fun interface Transition0: Transition {
  public fun execute(): Formula
}
public fun interface Transition1<in A>: Transition {
  public fun execute(x: Expr<A>): Formula
}
public fun interface Transition2<in A, in B>: Transition {
  public fun execute(x: Expr<A>, y: Expr<B>): Formula
}
public fun interface Transition3<in A, in B, in C>: Transition {
  public fun execute(x: Expr<A>, y: Expr<B>, z: Expr<C>): Formula
}
public fun interface Transition4<in A, in B, in C, in D>: Transition {
  public fun execute(arg1: Expr<A>, arg2: Expr<B>, arg3: Expr<C>, arg4: Expr<D>): Formula
}

@Target(AnnotationTarget.CLASS)
public annotation class initial
@Target(AnnotationTarget.CLASS)
public annotation class stutter
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class stutterFor(val klass: KClass<*>)