package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.codegen.core.directed.{CreateContextDirective, CreateSymbolProviderDirective, DirectedCodegen, GenerateEnumDirective, GenerateErrorDirective, GenerateIntEnumDirective, GenerateServiceDirective, GenerateStructureDirective, GenerateUnionDirective}

import java.util.logging.Logger

class ScalaPlayGenerator extends DirectedCodegen[ScalaPlayContext, ScalaPlaySettings, ScalaPlayIntegration] {
  val logger = Logger.getLogger(classOf[ScalaPlayGenerator].getName)

  override def createSymbolProvider(createSymbolProviderDirective: CreateSymbolProviderDirective[ScalaPlaySettings]): SymbolProvider =
    new ScalaPlaySymbolVisitor(createSymbolProviderDirective.model(),
      new ScalaSymbolVisitor(createSymbolProviderDirective.model(), createSymbolProviderDirective.settings()))

  override def createContext(createContextDirective: CreateContextDirective[ScalaPlaySettings, ScalaPlayIntegration]): ScalaPlayContext = ???

  override def generateService(generateServiceDirective: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???

  override def generateStructure(generateStructureDirective: GenerateStructureDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = {
    logger.info("generateStructure")
  }

  override def generateError(generateErrorDirective: GenerateErrorDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???

  override def generateUnion(generateUnionDirective: GenerateUnionDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???

  override def generateEnumShape(generateEnumDirective: GenerateEnumDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???

  override def generateIntEnumShape(generateIntEnumDirective: GenerateIntEnumDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???
}
