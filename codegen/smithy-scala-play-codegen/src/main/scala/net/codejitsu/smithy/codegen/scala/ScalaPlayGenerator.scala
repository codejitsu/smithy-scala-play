package net.codejitsu.smithy.codegen.scala

import net.codejitsu.smithy.codegen.scala.generators._
import software.amazon.smithy.codegen.core.directed._
import software.amazon.smithy.codegen.core.{Symbol, SymbolProvider}

import java.nio.file.Paths
import java.util.logging.Logger
import scala.jdk.CollectionConverters.CollectionHasAsScala

class ScalaPlayGenerator extends DirectedCodegen[ScalaPlayContext, ScalaPlaySettings, ScalaPlayIntegration] {
  private final val SERVICE_DIR: String = "app"
  private final val CONF_DIR: String = "conf"
  private final val BUILD_DIR: String = "project"

  val logger: Logger = Logger.getLogger(classOf[ScalaPlayGenerator].getName)

  override def createSymbolProvider(createSymbolProviderDirective: CreateSymbolProviderDirective[ScalaPlaySettings]): SymbolProvider = {
    new ScalaPlaySymbolVisitor(new ScalaSymbolVisitor(createSymbolProviderDirective.model(), createSymbolProviderDirective.settings()))
  }

  override def createContext(createContextDirective: CreateContextDirective[ScalaPlaySettings, ScalaPlayIntegration]): ScalaPlayContext = {
    val visitor = new ScalaSymbolVisitor(createContextDirective.model(), createContextDirective.settings())
    val symbolProvider = new ScalaPlaySymbolVisitor(visitor)

    ScalaPlayContext(createContextDirective.model(), createContextDirective.settings(), symbolProvider, createContextDirective.fileManifest(),
      new ScalaPlayDelegator(createContextDirective.fileManifest(), createContextDirective.symbolProvider()), createContextDirective.integrations())
  }

  override def generateService(generateServiceDirective: GenerateServiceDirective[ScalaPlayContext, ScalaPlaySettings]): Unit = {
    logger.info(s"[ScalaPlayGenerator]: start 'generateService'")

    val namespace =  generateServiceDirective.shape().getId.getNamespace.split("\\.")
    val service = generateServiceDirective.symbolProvider().toSymbol(generateServiceDirective.model().getServiceShapes.asScala.head)
    val handler = service.expectProperty("handler", classOf[Symbol])

    // base trait for business logic
    val pathBaseRules = namespace ++ Array("generated", "rules", s"${service.getName}Rules.scala") // TODO suffix in config
    val pathToFileBaseRulesTrait = Paths.get(SERVICE_DIR, pathBaseRules :_*).toString // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToFileBaseRulesTrait, (writer: ScalaPlayWriter) => {
      val generator = new BaseBusinessLogicTraitTraitCodegen(
        generateServiceDirective.shape(),
        generateServiceDirective.symbolProvider(),
        writer,
        generateServiceDirective.model())

      generator.generateBaseBusinessLogicTrait()
    })

    // default implementation
    val pathDefaultRules = namespace ++ Array("generated", "rules", s"${service.getName}RulesDefaultImpl.scala") // TODO suffix config
    val pathToFileDefaultRulesTrait = Paths.get(SERVICE_DIR, pathDefaultRules :_*).toString   // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToFileDefaultRulesTrait, (writer: ScalaPlayWriter) => {
      val generator = new DefaultBusinessLogicImplCodegen(
        generateServiceDirective.shape(),
        generateServiceDirective.symbolProvider(),
        writer,
        generateServiceDirective.model())

      generator.generateDefaultBusinessLogicImpl()
    })

    // main service controller
    val pathController = namespace ++ Array("generated", "controllers", s"${handler.getName}.scala")
    val pathToFileController = Paths.get(SERVICE_DIR, pathController :_*).toString  // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToFileController, (writer: ScalaPlayWriter) => {
      val generator = new ControllerCodegen(
        generateServiceDirective.shape(),
        generateServiceDirective.symbolProvider(),
        writer,
        generateServiceDirective.model())

      generator.generateController()
    })

    // default error handler
    val pathDefaultErrorHandler = namespace ++ Array("generated", "util", "ErrorHandler.scala")
    val pathToFileErrorHandler = Paths.get(SERVICE_DIR, pathDefaultErrorHandler :_*).toString  // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToFileErrorHandler, (writer: ScalaPlayWriter) => {
      val generator = new DefaultErrorHandlerCodegen(
        generateServiceDirective.shape(),
        writer)

      generator.generateDefaultErrorHandler()
    })

    // build sbt file
    val pathToFileSbt = Paths.get("build.sbt").toString // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToFileSbt, (writer: ScalaPlayWriter) => {
      val generator = new SbtCodegen(
        generateServiceDirective.shape(),
        generateServiceDirective.symbolProvider(),
        writer,
        generateServiceDirective.model())

      generator.generateSbt()
    })

    // application config
    val pathToConf = Paths.get(CONF_DIR, "application.conf").toString // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToConf, (writer: ScalaPlayWriter) => {
      val generator = new ConfigCodegen(
        generateServiceDirective.shape(),
        writer)

      generator.generate()
    })

    // routes
    val pathToRoutes = Paths.get(CONF_DIR, "routes").toString // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToRoutes, (writer: ScalaPlayWriter) => {
      val generator = new RoutesCodegen(
        generateServiceDirective.shape(),
        generateServiceDirective.symbolProvider(),
        writer,
        generateServiceDirective.model())

      generator.generateRoutes()
    })

    // build.properties
    val pathToBuildProperties = Paths.get(BUILD_DIR, "build.properties").toString // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToBuildProperties, (writer: ScalaPlayWriter) => {
      val generator = new BuildPropertiesCodegen(
        generateServiceDirective.shape(),
        writer)

      generator.generatePlayBuildProperties()
    })

    // plugins.sbt
    val pathToPluginsSbt = Paths.get(BUILD_DIR, "plugins.sbt").toString // TODO this belongs to config (project structure)

    generateServiceDirective.context().writerDelegator.useFileWriter(pathToPluginsSbt, (writer: ScalaPlayWriter) => {
      val generator = new PluginsSbtCodegen(
        generateServiceDirective.shape(),
        writer)

      generator.generatePluginsSbt()
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
