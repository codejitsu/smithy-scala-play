namespace net.codejitsu.smithy.codegen.scala

structure GetPokemonInput {
    @required
    name: String,
}

service PokemonService {
    version: "1.0.0",
    operations: [GetPokemon]
}

operation GetPokemon {
    input: GetPokemonInput
}