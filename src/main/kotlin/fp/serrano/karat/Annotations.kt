package fp.serrano.karat

// for classes
// - abstract represents 'abstract'
// - one represented 'one'
@Target(AnnotationTarget.CLASS)
annotation class abstract

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