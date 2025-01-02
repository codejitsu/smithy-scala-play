package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.directed.GenerateStructureDirective

import java.util.logging.Logger
import scala.jdk.CollectionConverters.IterableHasAsScala

object StructureCodegen {
  val logger: Logger = Logger.getLogger(classOf[StructureCodegen.type].getName)

  def generateCaseClass(directive: GenerateStructureDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter): Unit = {
    logger.info(s"[StructureCodegen]: start 'generate' for ${directive.shape.getId.getName}")

    val isInputShape = directive.model.getOperationShapes.asScala.exists(op => op.getInputShape == directive.shape.getId)
    val symbol = directive.symbolProvider.toSymbol(directive.shape)
    val structureFields = directive.shape.getAllMembers.values()

    writer.write("package ${L}.generated.models", directive.shape.getId.getNamespace)
    writer.write("")
    writer.write("import play.api.libs.json.{Json, OWrites}")

    if (isInputShape) {
      writer.write("import play.api.mvc.PathBindable")
    }

    writer.write("")

    writer.openBlock("case class $L(", symbol.getName)

    structureFields.asScala.zipWithIndex.foreach { case (field, index) =>
      val fieldName = field.getMemberName

      if (index != structureFields.size() - 1) {
        writer.write("${L}: ${T},", fieldName, directive.symbolProvider.toSymbol(field))
      } else {
        writer.write("${L}: ${T}", fieldName, directive.symbolProvider.toSymbol(field))
      }
    }

    writer.closeBlock(")")
    writer.write("")

    writer.openBlock("object $L {", symbol.getName)

    writer.write("implicit val ${L}Writes: OWrites[${L}] = Json.writes[${L}]",
      s"${symbol.getName.head.toLower}${symbol.getName.substring(1)}", symbol.getName, symbol.getName)

    if (isInputShape) {
      val identifier = directive.shape.getAllMembers.values().asScala.find(member => member.hasTrait("resourceIdentifier"))

      identifier.foreach { id =>
        writer.write("")

        val idType = directive.symbolProvider.toSymbol(id)

        writer.openBlock("implicit def pathBinder(implicit binder: PathBindable[$T]): PathBindable[$L] = new PathBindable[$L] {", idType, symbol.getName, symbol.getName)
        writer.openBlock("override def bind(key: String, value: String): Either[String, $L] = {", symbol.getName)
        writer.openBlock("for {")
        writer.write("field <- binder.bind(key, value)")
        writer.closeBlock("} yield $L(field)", symbol.getName)
        writer.closeBlock("}")
        writer.write("")
        writer.openBlock("override def unbind(key: String, $L: $L): String = {",
          s"${symbol.getName.head.toLower}${symbol.getName.substring(1)}", symbol.getName)
        writer.write("$L.$L", s"${symbol.getName.head.toLower}${symbol.getName.substring(1)}", id.getMemberName)
        writer.closeBlock("}")
        writer.closeBlock("}")
      }
    }

    writer.closeBlock("}")
  }
}
