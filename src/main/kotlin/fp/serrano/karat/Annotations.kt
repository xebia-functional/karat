package fp.serrano.karat

// for classes
// - abstract represents 'abstract'
// - one represented 'one'
@Target(AnnotationTarget.CLASS)
annotation class abstract
// the class represents a subset of other
@Target(AnnotationTarget.CLASS)
annotation class subset
// the class represents one element of other
@Target(AnnotationTarget.CLASS)
annotation class element
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class variable

// indicates that a field should be reflected
@Target(AnnotationTarget.PROPERTY)
annotation class reflect

// the cardinalities are chosen as follows:
// - nullable type -> lone
// - set type -> some, unless annotated
// - any other -> one

// cardinalities for set-based fields
@Target(AnnotationTarget.PROPERTY)
annotation class lone
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class one
@Target(AnnotationTarget.PROPERTY)
annotation class some

@Target(AnnotationTarget.CLASS)
annotation class initial
