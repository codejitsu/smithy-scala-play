$version: "2"

namespace net.codejitsu.smithy

@input
structure GetSomethingInput {
    @required
    @httpLabel
    id: Integer
}

@output
structure GetSomethingOutput {
    @required
    id: Integer

    age: Integer

    bool: Boolean

    byte: Byte

    short: Short

    long: Long

    float: Float

    double: Double

    bigInteger: BigInteger

    bigDecimal: BigDecimal

    timestamp: Timestamp
}

@readonly
@http(method: "GET", uri: "/primitives/{id}", code: 200)
operation GetSomething {
    input: GetSomethingInput
    output: GetSomethingOutput
}

service PrimitivesService {
    version: "1.0.0"
    operations: [
        GetSomething
    ]
}
