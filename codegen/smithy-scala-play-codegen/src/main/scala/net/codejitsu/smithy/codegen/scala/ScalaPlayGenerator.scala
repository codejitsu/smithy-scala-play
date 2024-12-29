package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.SymbolProvider
import software.amazon.smithy.codegen.core.directed.{CreateContextDirective, CreateSymbolProviderDirective, DirectedCodegen, GenerateEnumDirective, GenerateErrorDirective, GenerateIntEnumDirective, GenerateServiceDirective, GenerateStructureDirective, GenerateUnionDirective}

import java.util.logging.Logger

class ScalaPlayGenerator extends DirectedCodegen[ScalaPlayContext, ScalaPlaySettings, ScalaPlayIntegration] {
  val logger = Logger.getLogger(classOf[ScalaPlayGenerator].getName)

  override def createSymbolProvider(createSymbolProviderDirective: CreateSymbolProviderDirective[ScalaPlaySettings]): SymbolProvider = {
    new ScalaPlaySymbolVisitor(createSymbolProviderDirective.model(),
      new ScalaSymbolVisitor(createSymbolProviderDirective.model(), createSymbolProviderDirective.settings()))
  }

  override def createContext(createContextDirective: CreateContextDirective[ScalaPlaySettings, ScalaPlayIntegration]): ScalaPlayContext = {
    val visitor = new ScalaSymbolVisitor(createContextDirective.model(), createContextDirective.settings())
    val symbolProvider = new ScalaPlaySymbolVisitor(createContextDirective.model(), visitor)

    ScalaPlayContext(createContextDirective.model(), createContextDirective.settings(), symbolProvider, createContextDirective.fileManifest(),
      new ScalaPlayDelegator(createContextDirective.fileManifest(), createContextDirective.symbolProvider()), createContextDirective.integrations())
  }

  override def generateService(generateServiceDirective: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = {
    logger.info(s"[ScalaPlayGenerator]: start 'generateService'")

    generateServiceDirective.context().writerDelegator.useShapeWriter(generateServiceDirective.shape(), writer => {
      val generator = new ServiceCodegen(
        generateServiceDirective.shape(),
        generateServiceDirective.symbolProvider(),
        writer,
        generateServiceDirective.model(),
        generateServiceDirective.context().writerDelegator,
        generateServiceDirective)

      generator.generate()
    })
  }

  override def generateStructure(generateStructureDirective: GenerateStructureDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = {
    logger.info(s"[ScalaPlayGenerator]: start 'generateStructure'")

    generateStructureDirective.context().writerDelegator.useShapeWriter(generateStructureDirective.shape(), writer => {
      val generator = new StructureCodegen(
        generateStructureDirective.shape(),
        generateStructureDirective.symbolProvider(),
        writer,
        generateStructureDirective.model())

      generator.generate()
    })
  }

  override def generateError(generateErrorDirective: GenerateErrorDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???

  override def generateUnion(generateUnionDirective: GenerateUnionDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???

  override def generateEnumShape(generateEnumDirective: GenerateEnumDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???

  override def generateIntEnumShape(generateIntEnumDirective: GenerateIntEnumDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = ???
}
