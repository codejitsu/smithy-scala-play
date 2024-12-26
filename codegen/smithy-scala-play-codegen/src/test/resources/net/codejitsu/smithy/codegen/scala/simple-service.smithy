$version: "2"
namespace net.codejitsu.smithy

@input
structure GetPokemonInput {
    @required
    @httpLabel
    @resourceIdentifier("name")
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