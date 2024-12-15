package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.build.FileManifest
import software.amazon.smithy.codegen.core.{SymbolProvider, WriterDelegator}

class ScalaPlayDelegator(manifest: FileManifest, provider: SymbolProvider) extends WriterDelegator[ScalaPlayWriter](
  manifest, provider, ScalaPlayWriter.mkFactory()
  ) {

}
