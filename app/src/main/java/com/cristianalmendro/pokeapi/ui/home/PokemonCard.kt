package com.cristianalmendro.pokeapi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cristianalmendro.pokeapi.R
import com.cristianalmendro.pokeapi.data.Pokemon
import java.util.Locale

// Screenshot-based dimensions are grouped here for later manual tuning in Figma.
internal object HomeDimensions {
    val pagePadding = 32.dp
    val cardHeight = 144.dp
    val artworkSize = 136.dp
    val cardRadius = 10.dp
}

@Composable
internal fun PokemonCard(pokemon: Pokemon, modifier: Modifier = Modifier) {
    val style = PokemonTypeStyle.forName(pokemon.types.firstOrNull())
    val background = colorResource(style.background)
    Box(modifier.fillMaxWidth().height(HomeDimensions.cardHeight)
        .testTag("pokemon-card-${pokemon.id}").semantics(mergeDescendants = true) {}) {
        Box(Modifier.fillMaxSize().padding(top = 16.dp, bottom = 8.dp)
            .shadow(8.dp, RoundedCornerShape(HomeDimensions.cardRadius),
                ambientColor = background, spotColor = background)
            .background(background, RoundedCornerShape(HomeDimensions.cardRadius))) {
            AsyncImage(
                model = R.raw.pokemon_pattern, contentDescription = null,
                modifier = Modifier.align(Alignment.TopCenter).width(74.dp).height(32.dp),
            )
            AsyncImage(
                model = R.raw.pokemon_pokeball, contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd).width(130.dp).fillMaxHeight(),
                contentScale = ContentScale.Fit,
            )
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(.62f).padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = String.format(Locale.ROOT, "#%03d", pokemon.id),
                    color = colorResource(R.color.pokemon_number).copy(alpha = .6f),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                )
                Text(
                    text = pokemon.name.displayName(), color = Color.White,
                    fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pokemon.types.forEach { TypeBadge(it) }
                }
            }
        }
        AsyncImage(
            model = pokemon.artworkUrl, contentDescription = null,
            placeholder = painterResource(R.drawable.pokemon_loading),
            error = painterResource(R.drawable.pokemon_loading),
            fallback = painterResource(R.drawable.pokemon_loading),
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-6).dp)
                .size(HomeDimensions.artworkSize),
        )
    }
}

@Composable
private fun TypeBadge(name: String) {
    val style = PokemonTypeStyle.forName(name)
    Row(Modifier.background(colorResource(style.color), RoundedCornerShape(3.dp))
        .padding(horizontal = 5.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = style.icon, contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White), modifier = Modifier.size(13.dp))
        Text(name.displayName(), color = Color.White, fontSize = 12.sp,
            fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

internal fun String.displayName(): String = replace('-', ' ').replaceFirstChar { it.titlecase() }
