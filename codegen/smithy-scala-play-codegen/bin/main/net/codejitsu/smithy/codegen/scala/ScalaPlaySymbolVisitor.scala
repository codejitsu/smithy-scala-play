package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core
import software.amazon.smithy.codegen.core.{ReservedWordSymbolProvider, ReservedWordsBuilder, Symbol, SymbolProvider}
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.{Shape, ShapeVisitor}
import software.amazon.smithy.utils.StringUtils

import java.util.logging.Logger

class ScalaPlaySymbolVisitor(model: Model, symbolProvider: SymbolProvider) extends ShapeVisitor.Default[Symbol] with SymbolProvider {
  val logger = Logger.getLogger(classOf[ScalaPlaySymbolVisitor].getName)

  val escaper: ReservedWordSymbolProvider.Escaper = initEscaper()

  override def getDefault(shape: Shape): Symbol = symbolProvider.toSymbol(shape)

  override def toSymbol(shape: Shape): core.Symbol = {
    val symbol = shape.accept(this)

    logger.info(s"Mapping $shape to $symbol")

    escaper.escapeSymbol(shape, symbol)
  }

  private def initEscaper(): ReservedWordSymbolProvider.Escaper = {
    val reservedWords = new ReservedWordsBuilder()
      .loadWords(classOf[ScalaPlaySymbolVisitor].getResource("scala-reserved-words.txt"))
      .build()

    ReservedWordSymbolProvider.builder()
      .nameReservedWords(reservedWords)
      .escapePredicate((_, symbol) => !StringUtils.isEmpty(symbol.getDefinitionFile))
      .buildEscaper()
  }
}
