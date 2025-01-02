package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective

import java.util.logging.Logger

object PluginsSbtCodegen {
  val logger: Logger = Logger.getLogger(classOf[PluginsSbtCodegen.type].getName)

  def generatePluginsSbt(directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter): Unit = {
    logger.info(s"[PluginsSbtCodegen]: start 'generate' for ${directive.shape.getId.getName}")

    // TODO config
    // TODO mustache
    val content =
      s"""
         |addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.6")
         |addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.17.0")
         |""".stripMargin

    writer.write(content)
  }
}