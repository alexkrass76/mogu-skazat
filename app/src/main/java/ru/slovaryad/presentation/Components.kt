package ru.slovaryad.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.slovaryad.R

val Ink = Color(0xFF263F3A)
val Green = Color(0xFF316B61)
val Paper = Color(0xFFF6F7F2)
val Mint = Color(0xFFE4EFEB)
val Peach = Color(0xFFF5E9D9)
@Composable fun SlovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Green, onPrimary = Color.White,
        background = Paper, surface = Color.White, onSurface = Ink, onBackground = Ink,
        secondaryContainer = Mint, onSecondaryContainer = Ink), content = content)
}
private val illustrationResources = mapOf(
    "want" to R.drawable.art_want,
    "give" to R.drawable.art_give,
    "more" to R.drawable.art_more,
    "no" to R.drawable.art_no,
    "yes" to R.drawable.art_yes,
    "help" to R.drawable.art_help,
    "drink" to R.drawable.art_drink,
    "eat" to R.drawable.art_eat,
    "play" to R.drawable.art_play,
    "ball" to R.drawable.art_ball,
    "car" to R.drawable.art_car,
    "water" to R.drawable.art_water,
    "apple" to R.drawable.art_apple,
    "juice" to R.drawable.art_juice,
    "cat" to R.drawable.art_cat,
    "dog" to R.drawable.art_dog,
    "home" to R.drawable.art_home,
    "poppy" to R.drawable.art_poppy,
    "nose" to R.drawable.art_nose,
    "banana" to R.drawable.art_banana,
    "bear" to R.drawable.art_bear,
    "rest" to R.drawable.art_rest,
    "toilet" to R.drawable.art_toilet,
    "soup" to R.drawable.art_soup,
    "cheese" to R.drawable.art_cheese,
    "bread" to R.drawable.art_bread,
    "tea" to R.drawable.art_tea,
    "milk" to R.drawable.art_milk,
    "pear" to R.drawable.art_pear,
    "plum" to R.drawable.art_plum,
    "fish" to R.drawable.art_fish,
    "duck" to R.drawable.art_duck,
    "goose" to R.drawable.art_goose,
    "mouse" to R.drawable.art_mouse,
    "hare" to R.drawable.art_hare,
    "fox" to R.drawable.art_fox,
    "porridge" to R.drawable.art_porridge,
    "table" to R.drawable.art_table,
    "chair" to R.drawable.art_chair,
    "cup" to R.drawable.art_cup,
    "spoon" to R.drawable.art_spoon,
    "book" to R.drawable.art_book,
    "cube" to R.drawable.art_cube,
    "sleep" to R.drawable.art_sleep,
    "walk" to R.drawable.art_walk,
    "wash" to R.drawable.art_wash,
    "stop" to R.drawable.art_stop,
    "hand" to R.drawable.art_hand,
    "mouth" to R.drawable.art_mouth,
    "hedgehog" to R.drawable.art_hedgehog
)

fun illustrationResource(key: String): Int? = illustrationResources[key]

@Composable fun Illustration(key: String, label: String, modifier: Modifier = Modifier) {
    val id = illustrationResource(key)
    if (id != null) Image(painterResource(id), label, modifier)
    else Text("Картинка недоступна", modifier, color = Ink)
}
@Composable fun Action(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, modifier.heightIn(min = 64.dp), enabled = enabled, shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)) { Text(text, fontSize = 20.sp) }
}
@Composable fun Secondary(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick, modifier.heightIn(min = 60.dp), enabled = enabled, shape = RoundedCornerShape(18.dp)) { Text(text, fontSize = 18.sp) }
}
@Composable fun Heading(title: String, subtitle: String = "") {
    Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Ink)
    if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 18.sp, color = Ink.copy(alpha = .75f))
}
@Composable fun Info(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = Mint, shape = RoundedCornerShape(20.dp)) {
        Text(text, Modifier.padding(20.dp), fontSize = 18.sp, color = Ink)
    }
}
@Composable fun MenuTile(title: String, detail: String, art: String, large: Boolean, onClick: () -> Unit) {
    Card(onClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(if (large) 28.dp else 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(color = Peach, shape = RoundedCornerShape(20.dp)) { Illustration(art, nullLabel(title), Modifier.size(if (large) 100.dp else 80.dp).padding(8.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, fontSize = if (large) 30.sp else 26.sp, fontWeight = FontWeight.Bold)
                Text(detail, fontSize = 17.sp, color = Ink.copy(alpha = .75f))
            }
            Text("›", fontSize = 38.sp, color = Green)
        }
    }
}
private fun nullLabel(s: String) = s
