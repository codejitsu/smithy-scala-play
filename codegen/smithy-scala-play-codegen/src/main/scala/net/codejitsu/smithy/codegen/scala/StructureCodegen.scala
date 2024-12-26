package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.StructureShape

import java.util.logging.Logger
import scala.jdk.CollectionConverters.IterableHasAsScala

class StructureCodegen(
  val shape: StructureShape,
  val symbolProvider: SymbolProvider,
  val writer: ScalaPlayWriter,
  val model: Model) {
  val logger = Logger.getLogger(classOf[StructureCodegen].getName)

  def generate(): Unit = {
    logger.info(s"[StructureCodegen]: start 'generate' for ${shape.getId.getName}")

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
