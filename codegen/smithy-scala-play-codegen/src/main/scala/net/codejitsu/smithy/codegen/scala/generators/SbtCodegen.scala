package net.codejitsu.smithy.codegen.scala.generators

import net.codejitsu.smithy.codegen.scala.{ScalaPlayContext, ScalaPlaySettings, ScalaPlayWriter}
import software.amazon.smithy.codegen.core.directed.GenerateServiceDirective

import java.util.logging.Logger
import scala.jdk.CollectionConverters.CollectionHasAsScala

object SbtCodegen {
  val logger: Logger = Logger.getLogger(classOf[SbtCodegen.type].getName)

  def generateSbt(directive: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings], writer: ScalaPlayWriter): Unit = {
    logger.info(s"[SbtCodegen]: start 'generate' for ${directive.shape.getId.getName}")

    val service = directive.symbolProvider.toSymbol(directive.model.getServiceShapes.asScala.head)

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
  }
}
