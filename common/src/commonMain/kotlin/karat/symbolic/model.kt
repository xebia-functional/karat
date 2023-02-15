package karat.symbolic

import kotlin.reflect.KProperty

public object model {
  public operator fun <S, A> getValue(thisRef: S, property: KProperty<*>): A =
    throw IllegalStateException("this should never be de-referenced")
  public operator fun <S, A> setValue(thisRef: S, property: KProperty<*>, value: A) { }
}