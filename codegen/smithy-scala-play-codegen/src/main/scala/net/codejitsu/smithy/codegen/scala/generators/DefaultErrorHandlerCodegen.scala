package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.ScalaPlayWriter
import software.amazon.smithy.model.shapes.ServiceShape

import java.util.logging.Logger

class DefaultErrorHandlerCodegen(
  val serviceShape: ServiceShape,
  val writer: ScalaPlayWriter) {
  val logger: Logger = Logger.getLogger(classOf[DefaultErrorHandlerCodegen].getName)

  def generateDefaultErrorHandler(): Unit = {
    logger.info(s"[DefaultErrorHandlerCodegen]: start 'generate' for ${serviceShape.getId.getName}")

    writer.write("package ${L}.generated.util", serviceShape.getId.getNamespace)
    writer.write("")

    // TODO mustache
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
  }
}
