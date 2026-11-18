package com.cristianalmendro.pokeapi.data

enum class PokemonHeight {
    SHORT, MEDIUM, TALL;

    fun contains(decimetres: Int) = when (this) {
        SHORT -> decimetres < 10
        MEDIUM -> decimetres in 10 until 20
        TALL -> decimetres >= 20
    }
}

enum class PokemonWeight {
    LIGHT, MEDIUM, HEAVY;

    fun contains(hectograms: Int) = when (this) {
        LIGHT -> hectograms < 100
        MEDIUM -> hectograms in 100 until 1000
        HEAVY -> hectograms >= 1000
    }
}

data class TypeDefenses(
    val name: String,
    val doubleDamageFrom: List<String>,
    val halfDamageFrom: List<String>,
    val noDamageFrom: List<String>,
) {
    fun multiplier(attack: String): Double = when (attack) {
        in noDamageFrom -> 0.0
        in doubleDamageFrom -> 2.0
        in halfDamageFrom -> 0.5
        else -> 1.0
    }
}

data class PokemonFilters(
    val types: Set<String> = emptySet(),
    val weaknesses: Set<String> = emptySet(),
    val heights: Set<PokemonHeight> = emptySet(),
    val weights: Set<PokemonWeight> = emptySet(),
    val minimumId: Int = 1,
    val maximumId: Int? = null,
) {
    init {
        require(listOf(types.size, weaknesses.size, heights.size, weights.size).all { it <= 3 })
        require(minimumId >= 1 && (maximumId == null || maximumId >= minimumId))
    }

    val isActive get() = types.isNotEmpty() || weaknesses.isNotEmpty() ||
        heights.isNotEmpty() || weights.isNotEmpty() || minimumId > 1 || maximumId != null

    fun matches(pokemon: Pokemon, defenses: Map<String, TypeDefenses>): Boolean =
        pokemon.id >= minimumId && (maximumId == null || pokemon.id <= maximumId) &&
            (types.isEmpty() || pokemon.types.any { it in types }) &&
            (heights.isEmpty() || pokemon.heightDecimetres?.let { value -> heights.any { it.contains(value) } } == true) &&
            (weights.isEmpty() || pokemon.weightHectograms?.let { value -> weights.any { it.contains(value) } } == true) &&
            (weaknesses.isEmpty() || weaknesses.any { attack ->
                // Multiplication handles dual-type resistance cancellation and immunity.
                pokemon.types.isNotEmpty() && pokemon.types.all { it in defenses } &&
                    pokemon.types.fold(1.0) { value, type -> value * defenses.getValue(type).multiplier(attack) } > 1.0
            })
}

data class CachedPokemonResult(val pokemon: List<Pokemon>, val cachedCount: Int, val maximumId: Int)
