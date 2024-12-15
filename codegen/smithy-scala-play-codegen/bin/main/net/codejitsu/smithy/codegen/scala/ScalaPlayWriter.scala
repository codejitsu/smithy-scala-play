package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.SymbolWriter

class ScalaPlayWriter(importContainer: ImportDeclarations, module: String) extends
  SymbolWriter[ScalaPlayWriter, ImportDeclarations](importContainer) {
}

object ScalaPlayWriter {
  private class ScalaPlayWriterFactory extends SymbolWriter.Factory[ScalaPlayWriter] {
    override def apply(fileName: String, namespace: String): ScalaPlayWriter = {
      val module = if (fileName.endsWith(".scala")) { fileName.substring(0, fileName.length - 6) } else { fileName }
      new ScalaPlayWriter(new ImportDeclarations(), module)
    }
  }

  def mkFactory(): SymbolWriter.Factory[ScalaPlayWriter] = new ScalaPlayWriter.ScalaPlayWriterFactory()
}