package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective
import software.amazon.smithy.codegen.core.{Symbol, SymbolProvider, WriterDelegator}
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.knowledge.TopDownIndex
import software.amazon.smithy.model.shapes.ServiceShape
import software.amazon.smithy.model.traits.HttpTrait

import java.nio.file.Paths
import java.util.logging.Logger
import scala.jdk.CollectionConverters.SetHasAsScala

class ServiceCodegen(
  val shape: ServiceShape,
  val symbolProvider: SymbolProvider,
  val writer: ScalaPlayWriter,
  val model: Model,
  val writerDelegator: WriterDelegator[ScalaPlayWriter],
  val directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings]) {
  val logger = Logger.getLogger(classOf[ServiceCodegen].getName)

  def generate(): Unit = {
    logger.info(s"[ServiceCodegen]: start 'generate' for ${shape.getId.getName}")

    val namespace = shape.getId.getNamespace.split("\\.")
    val service = symbolProvider.toSymbol(model.getServiceShapes.asScala.head)
    val handler = service.expectProperty("handler", classOf[Symbol])

    // base trait
    val pathBaseRules = namespace ++ Array("generated", "rules", s"${service.getName}Rules.scala")
    val pathToFileBaseRulesTrait = Paths.get("app", pathBaseRules :_*).toString // TODO this belongs to config (project structure)

    //writerDelegator.useFileWriter(pathToFileBaseRulesTrait, (writer: ScalaPlayWriter) => {
      writer.write("package ${L}.generated.rules", shape.getId.getNamespace)
      writer.write("")
      writer.write("import com.google.inject.ImplementedBy")

      val operations = service.expectProperty("operations", classOf[Symbol])

      val index = TopDownIndex.of(model)

      val operationsShapes = index.getContainedOperations(model.getServiceShapes.asScala.head)

      val inputOutputModels = operationsShapes.asScala.flatMap { operation =>
        Seq(operation.getInputShape.getName, operation.getOutputShape.getName)
      }

      writer.write("import ${L}.generated.models.{${L}}", shape.getId.getNamespace, inputOutputModels.toSeq.sorted.mkString(", "))
      writer.write("")
      writer.write("@ImplementedBy(classOf[${L}RulesDefaultImpl])", service.getName) // TODO this suffix also belongs to config

      writer.openBlock("trait ${L}Rules {", service.getName)

      val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

      httpEndpoints.sorted.zipWithIndex.foreach { case (operation, i) =>
        val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

        writer.write("def ${L}(${L} ${L}): ${L}", methodName, s"${methodName}Input", operation.getInputShape.getName, operation.getOutputShape.getName)

        if (i != httpEndpoints.size - 1) {
          writer.write("")
        }
      }
      writer.closeBlock("}")
    //})
/*
    // default implementation
    val pathDefaultRules = namespace ++ Array("generated", "rules", s"${service.getName}RulesDefaultImpl.scala")
    val pathToFileDefaultRulesTrait = Paths.get("app", pathDefaultRules :_*).toString   // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToFileDefaultRulesTrait, (writer: ScalaPlayWriter) => {
      writer.write("package ${L}.generated.rules", shape.getId.getNamespace)
      writer.write("")

      val operations = service.expectProperty("operations", classOf[Symbol])

      val index = TopDownIndex.of(model)

      val operationsShapes = index.getContainedOperations(model.getServiceShapes.asScala.head)

      val inputOutputModels = operationsShapes.asScala.flatMap { operation =>
        Seq(operation.getInputShape.getName, operation.getOutputShape.getName)
      }

      writer.write("import ${L}.generated.models.{${L}}", shape.getId.getNamespace, inputOutputModels.toSeq.sorted.mkString(", "))
      writer.write("")
      writer.write("import javax.inject.Singleton")
      writer.write("")
      writer.write("@Singleton")

      writer.openBlock("class ${L}RulesDefaultImpl extends ${L}Rules {", service.getName, service.getName)

      val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

      httpEndpoints.sorted.zipWithIndex.foreach { case (operation, i) =>
        val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

        writer.openBlock("override def ${L}(${L} ${L}): ${L} = {", methodName, s"${methodName}Input", operation.getInputShape.getName, operation.getOutputShape.getName)
        writer.write("???")
        writer.closeBlock("}")

        if (i != httpEndpoints.size - 1) {
          writer.write("")
        }
      }
      writer.closeBlock("}")
    })

    // controller
    val pathController = namespace ++ Array("generated", "controllers", s"${handler.getName}.scala")
    val pathToFileController = Paths.get("app", pathController :_*).toString  // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToFileController, (writer: ScalaPlayWriter) => {
      writer.write("package ${L}.generated.controllers", shape.getId.getNamespace)
      writer.write("")

      val operations = service.expectProperty("operations", classOf[Symbol])

      val index = TopDownIndex.of(model)

      val operationsShapes = index.getContainedOperations(model.getServiceShapes.asScala.head)

      val inputOutputModels = operationsShapes.asScala.flatMap { operation =>
        Seq(operation.getInputShape.getName, operation.getOutputShape.getName)
      }

      writer.write("import ${L}.generated.models.{${L}}", shape.getId.getNamespace, inputOutputModels.toSeq.sorted.mkString(", "))
      writer.write("import ${L}.generated.rules.${L}Rules", shape.getId.getNamespace, service.getName)
      writer.write("import play.api.mvc._")
      writer.write("import javax.inject._")
      writer.write("import play.api.libs.json.Json")
      writer.write("")

      val outputWriters = operationsShapes.asScala.toSeq.sorted
        .map(_.getOutputShape.getName)
        .map { output =>
          val writer = s"${output.head.toLower}${output.substring(1)}Writes"
          s"import ${shape.getId.getNamespace}.generated.models.$output.$writer"
        }

      outputWriters.foreach(writer.write(_))
      writer.write("")

      val rules = s"${service.getName.head.toLower}${service.getName.substring(1)}Rules" // TODO suffix -> config

      writer.write("@Singleton")
      writer.openBlock("class ${L} @Inject()(val controllerComponents: ControllerComponents, val $L $L) extends BaseController {", handler.getName, rules, s"${service.getName}Rules")

      val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

      httpEndpoints.sorted.zipWithIndex.foreach { case (operation, i) =>
        val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

        writer.openBlock("def ${L}(${L} ${L}): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>", methodName, s"${methodName}Input", operation.getInputShape.getName)
        writer.write("val result = $L.$L($L)", rules, methodName, s"${methodName}Input")
        writer.write("Ok(Json.toJson(result))")

        writer.closeBlock("}")
        if (i != httpEndpoints.size - 1) {
          writer.write("")
        }
      }

      writer.closeBlock("}")
    })

    // default error handler
    val pathDefaultErrorHandler = namespace ++ Array("generated", "util", "ErrorHandler.scala")
    val pathToFileErrorHandler = Paths.get("app", pathDefaultErrorHandler :_*).toString  // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToFileErrorHandler, (writer: ScalaPlayWriter) => {
      writer.write("package ${L}.generated.util", shape.getId.getNamespace)
      writer.write("")

      val content =
        """
          |import javax.inject._
          |
          |import scala.concurrent._
          |
          |import play.api._
          |import play.api.http.DefaultHttpErrorHandler
          |import play.api.mvc._
          |import play.api.mvc.Results._
          |import play.api.routing.Router
          |
          |@Singleton
          |class ErrorHandler @Inject() (
          |     env: Environment,
          |     config: Configuration,
          |     sourceMapper: OptionalSourceMapper,
          |     router: Provider[Router]
          |   ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
          |  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
          |    Future.successful(
          |      NotImplemented
          |    )
          |  }
          |}
          |""".stripMargin

      writer.write(content)
    })

    // sbt
    val pathToFileSbt = Paths.get("build.sbt").toString // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToFileSbt, (writer: ScalaPlayWriter) => {
      // TODO config
      // TODO mustache
      val content =
        s"""
          |name := "${service.getName}"
          |organization := "${service.getName}"
          |
          |version := "1.0-SNAPSHOT"
          |
          |lazy val root = (project in file(".")).enablePlugins(PlayScala)
          |
          |scalaVersion := "2.13.15"
          |
          |libraryDependencies += guice
          |libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0" % Test
          |""".stripMargin

      writer.write(content)
    })

    // config
    val pathToConf = Paths.get("conf", "application.conf").toString // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToConf, (writer: ScalaPlayWriter) => {
      // TODO config
      val content =
        s"""
           |play.http.errorHandler = "${shape.getId.getNamespace}.generated.util.ErrorHandler"
           |""".stripMargin

      writer.write(content)
    })

    // routes
    val pathToRoutes = Paths.get("conf", "routes").toString // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToRoutes, (writer: ScalaPlayWriter) => {
      val index = TopDownIndex.of(model)

      val operationsShapes = index.getContainedOperations(model.getServiceShapes.asScala.head)

      val httpEndpoints = operationsShapes.asScala.toSeq.filter(_.hasTrait("http"))

      httpEndpoints.sorted.zipWithIndex.foreach { case (operation, _) =>
        val httpTrait = operation.getTrait(classOf[HttpTrait])

        val methodName = s"${operation.getId.getName.head.toLower}${operation.getId.getName.substring(1)}"

        writer.write("$L      $L      $L.generated.controllers.${L}.$L($L: $L)", httpTrait.get().getMethod,
          httpTrait.get().getUri.toString.replace("{", ":").replace("}", ""),
          shape.getId.getNamespace, handler.getName, methodName, httpTrait.get().getUri.getLabels.get(0).toString.replace("{", "").replace("}", ""),
          s"${shape.getId.getNamespace}.generated.models.${operation.getInputShape.getName}")
      }
    })

    // build.properties
    val pathToBuildProperties = Paths.get("project", "build.properties").toString // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToBuildProperties, (writer: ScalaPlayWriter) => {
      // TODO config
      // TODO mustache
      val content =
        s"""
           |sbt.version=1.10.6
           |""".stripMargin

      writer.write(content)
    })

    // plugins.sbt
    val pathToPluginsSbt = Paths.get("project", "plugins.sbt").toString // TODO this belongs to config (project structure)

    writerDelegator.useFileWriter(pathToPluginsSbt, (writer: ScalaPlayWriter) => {
      // TODO config
      // TODO mustache
      val content =
        s"""
           |addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.6")
           |addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.17.0")
           |""".stripMargin

      writer.write(content)
    })
*/
  }
}
