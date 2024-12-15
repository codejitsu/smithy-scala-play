package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.StructureShape

import scala.jdk.CollectionConverters.IterableHasAsScala

class StructureCodegen(
  val shape: StructureShape,
  val symbolProvider: SymbolProvider,
  val writer: ScalaPlayWriter,
  val model: Model) {
  def generate(): Unit = {
    val symbol = symbolProvider.toSymbol(shape)

    writer.openBlock("case class $L (", symbol.getName)

    val structureFields = shape.getAllMembers.values()

    structureFields.asScala.foreach { field =>
      val fieldName = field.getMemberName

      writer.write("${L}: ${T}", fieldName, symbolProvider.toSymbol(field))
    }

    writer.closeBlock(")")
    writer.write("")
  }
}
