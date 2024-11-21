package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.build.{FileManifest, PluginContext, SmithyBuildPlugin}
import software.amazon.smithy.codegen.core.directed.CodegenDirector
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.ServiceShape

class SmithyScalaPlayCodegenPlugin extends SmithyBuildPlugin {
  override def getName: String = "scala-play-server-codegen"

  override def execute(pluginContext: PluginContext): Unit = {
    val model = pluginContext.getModel
    val manifest = pluginContext.getFileManifest

    val services = model.shapes().filter(_.isServiceShape)

    services.forEach { case service: ServiceShape =>
      triggerServiceCodeGenerationFor(service, model, manifest)
    }
  }

  private def triggerServiceCodeGenerationFor(service: ServiceShape, model: Model, manifest: FileManifest): Unit = {
    val codegenDirector = new CodegenDirector[ScalaPlayWriter, ScalaPlayIntegration, ScalaPlayContext, ScalaPlaySettings]
  }
}
