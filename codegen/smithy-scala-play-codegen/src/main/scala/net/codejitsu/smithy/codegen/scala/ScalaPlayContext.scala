package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.build.FileManifest
import software.amazon.smithy.codegen.core.{CodegenContext, SymbolProvider, WriterDelegator}
import software.amazon.smithy.model.Model

import java.util

case class ScalaPlayContext(
    model: Model,
    settings: ScalaPlaySettings,
    symbolProvider: SymbolProvider,
    fileManifest: FileManifest,
    writerDelegator: WriterDelegator[ScalaPlayWriter],
    integrations: util.List[ScalaPlayIntegration]
  ) extends CodegenContext[ScalaPlaySettings, ScalaPlayWriter, ScalaPlayIntegration] { }
