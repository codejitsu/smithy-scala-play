$version: "2"
namespace net.codejitsu.smithy.codegen.scala

@input
structure GetPokemonInput {
    @required
    @httpLabel
    name: String,
}

@output
structure GetPokemonOutput {
    @required
    name: String,

    @required
    age: Integer
}

service PokemonService {
    version: "1.0.0",
    operations: [GetPokemon]
}

@readonly
@http(method: "GET", uri: "/pokemons/{name}", code: 200)
operation GetPokemon {
    input: GetPokemonInput,
    output: GetPokemonOutput
}