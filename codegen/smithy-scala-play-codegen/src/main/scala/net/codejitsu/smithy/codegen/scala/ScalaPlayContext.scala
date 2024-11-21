package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.build.FileManifest
import software.amazon.smithy.codegen.core.{CodegenContext, SymbolProvider, WriterDelegator}
import software.amazon.smithy.model.Model

import java.util

class ScalaPlayContext extends CodegenContext[ScalaPlaySettings, ScalaPlayWriter, ScalaPlayIntegration] {
  override def model(): Model = ???

  override def settings(): ScalaPlaySettings = ???

  override def symbolProvider(): SymbolProvider = ???

  override def fileManifest(): FileManifest = ???

  override def writerDelegator(): WriterDelegator[ScalaPlayWriter] = ???

  override def integrations(): util.List[ScalaPlayIntegration] = ???
}
