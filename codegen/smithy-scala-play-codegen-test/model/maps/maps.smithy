$version: "2"

namespace net.codejitsu.smithy

service ExampleMaps {
    version: "1.0.0"
    operations: [
        GetFoo
    ]
}

@readonly
@http(method: "GET", uri: "/getfoo/{id}", code: 200)
operation GetFoo {
    input: GetFooInput
    output: GetFooOutput
}

@input
structure GetFooMapsInput {
    @required
    @httpLabel
    id: Integer
}

@output
structure GetFooMapsOutput {
    foo: NamesMap
}

map NamesMap {
    key: String
    value: Integer
}
