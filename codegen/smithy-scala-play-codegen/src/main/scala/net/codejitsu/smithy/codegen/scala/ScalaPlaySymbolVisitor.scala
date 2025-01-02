package net.codejitsu.smithy.codegen.scala

import software.amazon.smithy.codegen.core.{ReservedWordSymbolProvider, ReservedWordsBuilder, Symbol, SymbolDependency, SymbolProvider}
import software.amazon.smithy.model.shapes.{OperationShape, ServiceShape, Shape, ShapeVisitor}
import software.amazon.smithy.utils.StringUtils

import java.util.logging.Logger

class ScalaPlaySymbolVisitor(symbolProvider: SymbolProvider) extends ShapeVisitor.Default[Symbol] with SymbolProvider {
  val logger: Logger = Logger.getLogger(classOf[ScalaPlaySymbolVisitor].getName)

  private val escaper: ReservedWordSymbolProvider.Escaper = initEscaper()

  override def getDefault(shape: Shape): Symbol = symbolProvider.toSymbol(shape)

  override def toSymbol(shape: Shape): Symbol = {
    logger.info(s"[ScalaPlaySymbolVisitor]: start 'toSymbol' for ${shape.getId.getName}")

    val symbol = shape.accept(this)

    logger.info(s"[ScalaPlaySymbolVisitor]: mapping $shape to $symbol")

    escaper.escapeSymbol(shape, symbol)
  }

  override def serviceShape(shape: ServiceShape): Symbol = {
    logger.info(s"[ScalaPlaySymbolVisitor]: start 'serviceShape' for ${shape.getId.getName}")

    val service = shape.getId.getName
    val namespace = shape.getId.getNamespace

    val intermediate = Symbol.builder()
      .putProperty("shape", shape)
      .name(service)
      .namespace(namespace, ".")
      .build()

    val smithyServerDependency = SymbolDependency.builder()
      .dependencyType("dependencies")
      .packageName("@aws-smithy/server-common")
      .version("1.53.0") // TODO detect version automatically
      .putProperty("unconditional", false)
      .build()

    val builder = intermediate.toBuilder.addDependency(smithyServerDependency)

    builder.putProperty("operations",
      intermediate.toBuilder.name(service + "Operations").build())
    builder.putProperty("handler",
      intermediate.toBuilder.name(service + "Controller").build())
    builder.build()
  }

  override def operationShape(shape: OperationShape): Symbol = {
    logger.info(s"[ScalaPlaySymbolVisitor]: start 'operationShape' for ${shape.getId.getName}")

    val name = shape.getId.getName
    val namespace = shape.getId.getNamespace

    val intermediate = Symbol.builder()
      .putProperty("shape", shape)
      .name(name)
      .namespace(namespace, ".")
      .build()

    val builder = intermediate.toBuilder

    builder.putProperty("inputType", intermediate.toBuilder.name(name + "SmithyInput").build())
    builder.putProperty("outputType", intermediate.toBuilder.name(name + "SmithyOutput").build())
    builder.putProperty("errorsType", intermediate.toBuilder.name(name + "SmithyErrors").build())
    builder.putProperty("serializerType", intermediate.toBuilder.name(name + "SmithySerializer").build())
    builder.putProperty("handler",
      intermediate.toBuilder.name(name + "SmithyHandler").build())

    builder.build()
  }

  private def initEscaper(): ReservedWordSymbolProvider.Escaper = {
    val reservedWords = new ReservedWordsBuilder()
      .loadWords(classOf[ScalaPlaySymbolVisitor].getResource("scala-reserved-words.txt"))
      .build()

    ReservedWordSymbolProvider.builder()
      .nameReservedWords(reservedWords)
      .escapePredicate((_, symbol) => !StringUtils.isEmpty(symbol.getDefinitionFile))
      .buildEscaper()
  }
}
