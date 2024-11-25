package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core
import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.{Shape, ShapeVisitor}

class ScalaPlaySymbolVisitor(model: Model, visitor: ScalaSymbolVisitor) extends ShapeVisitor.Default[Symbol] with SymbolProvider {
  override def getDefault(shape: Shape): Symbol = ???

  override def toSymbol(shape: Shape): core.Symbol = ???
}
