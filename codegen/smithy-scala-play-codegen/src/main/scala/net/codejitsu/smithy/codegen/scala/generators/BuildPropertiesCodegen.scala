package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective

import java.util.logging.Logger

object BuildPropertiesCodegen {
  val logger: Logger = Logger.getLogger(classOf[BuildPropertiesCodegen.type].getName)

  def generatePlayBuildProperties(directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter): Unit = {
    logger.info(s"[BuildPropertiesCodegen]: start 'generate' for ${directive.shape.getId.getName}")

    // TODO config
    // TODO mustache
    val content =
      s"""
         |sbt.version=1.10.6
         |""".stripMargin

    writer.write(content)
  }
}