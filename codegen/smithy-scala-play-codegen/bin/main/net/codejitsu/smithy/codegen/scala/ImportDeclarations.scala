package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.Symbol
import software.amazon.smithy.codegen.core.ImportContainer
import software.amazon.smithy.utils.StringUtils
import scala.collection.mutable

class ImportDeclarations extends ImportContainer {
  private val imports = mutable.Map.empty[String, String]

  override def importSymbol(symbol: Symbol, alias: String): Unit = {
    if (!StringUtils.isEmpty(symbol.getNamespace)) {
      // alias -> symbol
      imports.addOne(alias -> symbol.getName)
    }
  }
}
