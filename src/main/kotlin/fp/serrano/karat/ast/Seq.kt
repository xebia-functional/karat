package fp.serrano.karat.ast

import edu.mit.csail.sdg.ast.Sig

fun <A> seq(s: KSet<A>): KSet<List<A>> =
  KSet(Sig.PrimSig.SEQIDX.isSeq_arrow_lone(s.expr))
