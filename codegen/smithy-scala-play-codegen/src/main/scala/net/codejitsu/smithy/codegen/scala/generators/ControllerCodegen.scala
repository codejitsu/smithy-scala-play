package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.Symbol
import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective
import software.amazon.smithy.model.knowledge.TopDownIndex

import java.util.logging.Logger
import scala.jdk.CollectionConverters.SetHasAsScala

object ControllerCodegen {
  val logger: Logger = Logger.getLogger(classOf[ControllerCodegen.type].getName)

  def generateController(directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter): Unit = {
    logger.info(s"[ControllerCodegen]: start 'generate' for ${directive.shape.getId.getName}")

    val service = directive.symbolProvider.toSymbol(directive.model.getServiceShapes.asScala.head)
    val handler = service.expectProperty("handler", classOf[Symbol])

    val index = TopDownIndex.of(directive.model)

    val operationsShapes = index.getContainedOperations(directive.model.getServiceShapes.asScala.head)

    val inputOutputModels = operationsShapes.asScala.flatMap { operation =>
      Seq(operation.getInputShape.getName, operation.getOutputShape.getName)
    }

    writer.write("package ${L}.generated.controllers", directive.shape.getId.getNamespace)
    writer.write("")
    writer.write("import ${L}.generated.models.{${L}}", directive.shape.getId.getNamespace, inputOutputModels.toSeq.sorted.mkString(", "))
    writer.write("import ${L}.generated.rules.${L}Rules", directive.shape.getId.getNamespace, service.getName)
    writer.write("import play.api.mvc._")
    writer.write("import javax.inject._")
    writer.write("import play.api.libs.json.Json")
    writer.write("")

    val outputWriters = operationsShapes.asScala.toSeq.sorted
      .map(_.getOutputShape.getName)
      .map { output =>
        val writesObject = s"${output.head.toLower}${output.substring(1)}Writes"
        s"import ${directive.shape.getId.getNamespace}.generated.models.$output.$writesObject"
      }

    outputWriters.foreach(writer.write(_))
    writer.write("")

    val rules = s"${service.getName.head.toLower}${service.getName.substring(1)}Rules" // TODO suffix -> config

    writer.write("@Singleton")
    writer.openBlock("class ${L} @Inject()(val controllerComponents: ControllerComponents, val $L: $L) extends BaseController {", handler.getName, rules, s"${service.getName}Rules")

    val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

    httpEndpoints.sorted.zipWithIndex.foreach { case (operation, i) =>
      val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

      writer.openBlock("def ${L}(${L}: ${L}): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>", methodName, s"${methodName}Input", operation.getInputShape.getName)
      writer.write("val result = $L.$L($L)", rules, methodName, s"${methodName}Input")
      writer.write("Ok(Json.toJson(result))")

      writer.closeBlock("}")
      if (i != httpEndpoints.size - 1) {
        writer.write("")
      }
    }

    writer.closeBlock("}")
  }
}
