package com.cristianalmendro.pokeapi.ui.home

import androidx.annotation.ColorRes
import androidx.annotation.RawRes
import com.cristianalmendro.pokeapi.R

internal enum class PokemonTypeStyle(
    @field:ColorRes val color: Int,
    @field:ColorRes val background: Int,
    @field:RawRes val icon: Int,
) {
    BUG(R.color.pokemon_bug, R.color.pokemon_back_bug, R.raw.pokemon_bug),
    DARK(R.color.pokemon_dark, R.color.pokemon_back_dark, R.raw.pokemon_dark),
    DRAGON(R.color.pokemon_dragon, R.color.pokemon_back_dragon, R.raw.pokemon_dragon),
    ELECTRIC(R.color.pokemon_electric, R.color.pokemon_back_electric, R.raw.pokemon_electric),
    FAIRY(R.color.pokemon_fairy, R.color.pokemon_back_fairy, R.raw.pokemon_fairy),
    FIGHTING(R.color.pokemon_fighting, R.color.pokemon_back_fighting, R.raw.pokemon_fighting),
    FIRE(R.color.pokemon_fire, R.color.pokemon_back_fire, R.raw.pokemon_fire),
    FLYING(R.color.pokemon_flying, R.color.pokemon_back_flying, R.raw.pokemon_flying),
    GHOST(R.color.pokemon_ghost, R.color.pokemon_back_ghost, R.raw.pokemon_ghost),
    GRASS(R.color.pokemon_grass, R.color.pokemon_back_grass, R.raw.pokemon_grass),
    GROUND(R.color.pokemon_ground, R.color.pokemon_back_ground, R.raw.pokemon_ground),
    ICE(R.color.pokemon_ice, R.color.pokemon_back_ice, R.raw.pokemon_ice),
    NORMAL(R.color.pokemon_normal, R.color.pokemon_back_normal, R.raw.pokemon_normal),
    POISON(R.color.pokemon_poison, R.color.pokemon_back_poison, R.raw.pokemon_poison),
    PSYCHIC(R.color.pokemon_psychic, R.color.pokemon_back_psychic, R.raw.pokemon_psychic),
    ROCK(R.color.pokemon_rock, R.color.pokemon_back_rock, R.raw.pokemon_rock),
    STEEL(R.color.pokemon_steel, R.color.pokemon_back_steel, R.raw.pokemon_steel),
    WATER(R.color.pokemon_water, R.color.pokemon_back_water, R.raw.pokemon_water);

    companion object {
        fun forName(name: String?) = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: NORMAL
    }
}
