package karat.scalacheck

import karat.concrete.*
import org.scalacheck.Prop

type ScalacheckAtomic[A] = Atomic[A, Prop.Result]
type ScalacheckFormula[A] = Formula[A, Prop.Result]