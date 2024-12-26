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

    val isInputShape = model.getOperationShapes.asScala.exists(op => op.getInputShape == shape.getId)

    val symbol = symbolProvider.toSymbol(shape)

    writer.write("package ${L}.generated.models", shape.getId.getNamespace)
    writer.write("")
    writer.write("import play.api.libs.json.{Json, OWrites}")
    writer.write("")

    if (isInputShape) {
      writer.write("import play.api.mvc.PathBindable")
    }

    writer.openBlock("case class $L(", symbol.getName)

    val structureFields = shape.getAllMembers.values()

    structureFields.asScala.zipWithIndex.foreach { case (field, index) =>
      val fieldName = field.getMemberName

      if (index != structureFields.size() - 1) {
        writer.write("${L}: ${T},", fieldName, symbolProvider.toSymbol(field))
      } else {
        writer.write("${L}: ${T}", fieldName, symbolProvider.toSymbol(field))
      }
    }

    writer.closeBlock(")")
    writer.write("")

    writer.openBlock("object $L {", symbol.getName)

    writer.write("implicit val ${L}Writes: OWrites[${L}] = Json.writes[${L}]",
      s"${symbol.getName.head.toLower}${symbol.getName.substring(1)}", symbol.getName, symbol.getName)

    writer.closeBlock("}")
  }
}
