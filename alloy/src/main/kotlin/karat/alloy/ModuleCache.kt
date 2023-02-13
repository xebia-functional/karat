package karat.alloy

import edu.mit.csail.sdg.alloy4.Util
import edu.mit.csail.sdg.ast.*
import edu.mit.csail.sdg.parser.CompModule
import edu.mit.csail.sdg.parser.CompUtil
import java.io.File

public data class CachedModule(
  val sigs: List<Sig>,
  val facts: List<Expr>,
  val funcs: List<Func> = emptyList(),
  val preds: List<Func> = emptyList()
) {
  public operator fun get(name: String): Func? =
    (funcs + preds).find { it.label == name || it.label == "this/$name" }
}

public class ModuleCache {
  private val moduleCache: MutableMap<String, CachedModule> = mutableMapOf()

  public operator fun get(name: String): CachedModule? {
    if (!moduleCache.containsKey(name)) {
      val m = loadModule(name)
      moduleCache[name] = CachedModule(
        m.allSigs.toList(),
        m.allFacts.map { it.b },
        m.allFunc.filter { !it.isPred },
        m.allFunc.filter { it.isPred }
      )
    }
    return moduleCache[name]
  }
}

private fun loadModule(name: String): CompModule {
  val newCp = "${Util.jarPrefix()}models/${name}.als".replace('/', File.separatorChar)
  return CompUtil.parseEverything_fromFile(null, mutableMapOf(), newCp)
}