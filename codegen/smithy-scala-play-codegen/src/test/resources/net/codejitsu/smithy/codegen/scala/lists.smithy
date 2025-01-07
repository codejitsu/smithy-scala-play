$version: "2"

namespace net.codejitsu.smithy

service Example {
    version: "1.0.0",
    operations: [GetFoo]
}

@http(method: "GET", uri: "/getfoo/{id}", code: 200)
operation GetFoo {
    input: GetFooInput,
    output: GetFooOutput
}

@input
structure GetFooInput {
    @required
    @httpLabel
    id: Integer
}

@output
structure GetFooOutput {
    foo: NamesList
}

list NamesList {
    member: String
}

