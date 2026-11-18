package com.cristianalmendro.pokeapi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cristianalmendro.pokeapi.R
import com.cristianalmendro.pokeapi.data.PokemonFilters
import com.cristianalmendro.pokeapi.data.PokemonHeight
import com.cristianalmendro.pokeapi.data.PokemonWeight
import java.util.Locale
import kotlin.math.roundToInt

private val FilterAccent = Color(0xFFEA5D60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonFilterSheet(
    state: HomeUiState,
    onDraftChange: (PokemonFilters) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val filters = state.draftFilters
    val enabled = !state.isPreparingFilters
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).testTag("filters-sheet")) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.filters_title), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.filters_description), color = colorResource(R.color.pokemon_gray),
                    fontSize = 14.sp)
                Text(stringResource(R.string.filters_limit), color = colorResource(R.color.pokemon_gray), fontSize = 12.sp)
                FilterHeading(R.string.filters_types)
                TypeChoices("type", filters.types, enabled) { onDraftChange(filters.copy(types = it)) }
                FilterHeading(R.string.filters_weaknesses)
                TypeChoices("weakness", filters.weaknesses, enabled) { onDraftChange(filters.copy(weaknesses = it)) }
                FilterHeading(R.string.filters_heights)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PokemonHeight.entries.forEach { height ->
                        val title = stringResource(when (height) {
                            PokemonHeight.SHORT -> R.string.height_short
                            PokemonHeight.MEDIUM -> R.string.height_medium
                            PokemonHeight.TALL -> R.string.height_tall
                        })
                        val color = when (height) {
                            PokemonHeight.SHORT -> Color(0xFFFFC5E6)
                            PokemonHeight.MEDIUM -> Color(0xFFAEBFD7)
                            PokemonHeight.TALL -> Color(0xFFAAACB8)
                        }
                        FilterChoice(title, height.name.take(1), height in filters.heights, color,
                            enabled, "height-${height.name.lowercase(Locale.ROOT)}") {
                            onDraftChange(filters.copy(heights = filters.heights.toggled(height)))
                        }
                    }
                }
                FilterHeading(R.string.filters_weights)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PokemonWeight.entries.forEach { weight ->
                        val title = stringResource(when (weight) {
                            PokemonWeight.LIGHT -> R.string.weight_light
                            PokemonWeight.MEDIUM -> R.string.weight_medium
                            PokemonWeight.HEAVY -> R.string.weight_heavy
                        })
                        val color = when (weight) {
                            PokemonWeight.LIGHT -> Color(0xFF99CD7C)
                            PokemonWeight.MEDIUM -> Color(0xFF57B2DC)
                            PokemonWeight.HEAVY -> Color(0xFF5A92A5)
                        }
                        FilterChoice(title, weight.name.take(1), weight in filters.weights, color,
                            enabled, "weight-${weight.name.lowercase(Locale.ROOT)}") {
                            onDraftChange(filters.copy(weights = filters.weights.toggled(weight)))
                        }
                    }
                }
                FilterHeading(R.string.filters_number_range)
                RangeSlider(value = filters.minimumId.toFloat()..
                    (filters.maximumId ?: state.filterMaximumId).toFloat().coerceAtMost(state.filterMaximumId.toFloat()),
                    onValueChange = { range ->
                        val minimum = range.start.roundToInt()
                        val maximum = range.endInclusive.roundToInt()
                        onDraftChange(filters.copy(minimumId = minimum,
                            maximumId = maximum.takeUnless { it == state.filterMaximumId }))
                    }, valueRange = 1f..state.filterMaximumId.toFloat(), enabled = enabled,
                    colors = SliderDefaults.colors(thumbColor = FilterAccent, activeTrackColor = FilterAccent),
                    modifier = Modifier.testTag("filter-number-range"))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(filters.minimumId.toString())
                    Text((filters.maximumId ?: state.filterMaximumId).toString())
                }
                Text(stringResource(R.string.filters_cache_note, state.cachedCount),
                    color = colorResource(R.color.pokemon_gray), fontSize = 12.sp)
                if (state.isPreparingFilters) {
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = FilterAccent)
                    Text(stringResource(R.string.filters_preparing), fontSize = 12.sp)
                }
                if (state.filterError) Text(stringResource(R.string.filters_error), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(4.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onReset, enabled = enabled, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF2F2F2), contentColor = colorResource(R.color.pokemon_gray))) {
                    Text(stringResource(R.string.filters_reset))
                }
                Button(onClick = onApply, enabled = enabled && !state.isLoading,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("apply-filters"),
                    shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = FilterAccent)) {
                    Text(stringResource(R.string.filters_apply))
                }
            }
        }
    }
}

@Composable
private fun FilterHeading(resource: Int) {
    Text(stringResource(resource), fontSize = 16.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun TypeChoices(group: String, selected: Set<String>, enabled: Boolean, onChange: (Set<String>) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        PokemonTypeStyle.entries.forEach { type ->
            val name = type.name.lowercase(Locale.ROOT)
            val title = name.replaceFirstChar { it.titlecase(Locale.ROOT) }
            val chosen = name in selected
            Column(Modifier.width(58.dp).toggleable(value = chosen,
                enabled = enabled && (chosen || selected.size < 3), role = Role.Checkbox,
                onValueChange = { onChange(selected.toggled(name)) }).testTag("$group-$name")
                .semantics { contentDescription = title }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.padding(4.dp).size(42.dp)
                    .shadow(if (chosen) 6.dp else 0.dp, CircleShape)
                    .background(if (chosen) colorResource(type.color) else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center) {
                    AsyncImage(type.icon, contentDescription = null,
                        colorFilter = ColorFilter.tint(if (chosen) Color.White else colorResource(type.color)),
                        modifier = Modifier.size(24.dp))
                }
                Text(title, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun FilterChoice(title: String, symbol: String, selected: Boolean, color: Color,
    enabled: Boolean, tag: String, onClick: () -> Unit) {
    Column(Modifier.width(72.dp).toggleable(value = selected, enabled = enabled, role = Role.Checkbox,
        onValueChange = { onClick() }).testTag(tag).semantics { contentDescription = title },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(44.dp).shadow(if (selected) 5.dp else 0.dp, CircleShape)
            .background(if (selected) color else Color(0xFFF2F2F2), CircleShape), contentAlignment = Alignment.Center) {
            Text(symbol, color = if (selected) Color.White else color, fontWeight = FontWeight.Bold)
        }
        Text(title, fontSize = 10.sp)
    }
}

private fun <T> Set<T>.toggled(value: T): Set<T> = when {
    value in this -> this - value
    size < 3 -> this + value
    else -> this
}
