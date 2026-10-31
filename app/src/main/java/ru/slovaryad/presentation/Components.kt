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
@Composable fun Illustration(key: String, label: String, modifier: Modifier = Modifier) {
    val id = when(key) {
        "cat" -> R.drawable.art_cat; "dog" -> R.drawable.art_dog; "ball" -> R.drawable.art_ball
        "water" -> R.drawable.art_water; "apple" -> R.drawable.art_apple; "juice" -> R.drawable.art_juice
        "car" -> R.drawable.art_car; "home" -> R.drawable.art_home; "nose" -> R.drawable.art_nose
        "poppy" -> R.drawable.art_poppy; "banana" -> R.drawable.art_banana; "bear" -> R.drawable.art_bear
        "yes" -> R.drawable.art_yes; "no" -> R.drawable.art_no; "more" -> R.drawable.art_more
        "help" -> R.drawable.art_help; "give" -> R.drawable.art_give; "want" -> R.drawable.art_want
        "eat" -> R.drawable.art_eat; "drink" -> R.drawable.art_drink; "play" -> R.drawable.art_play
        "rest" -> R.drawable.art_rest; else -> R.drawable.art_toilet
    }
    Image(painterResource(id), label, modifier)
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
