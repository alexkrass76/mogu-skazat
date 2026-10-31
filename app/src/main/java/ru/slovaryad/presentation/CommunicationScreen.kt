package ru.slovaryad.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.slovaryad.domain.*
import java.time.LocalDate

@Composable fun CommunicationScreen(words: List<Word>, s: Settings, speak: (String) -> Unit, record: (Attempt) -> Unit) {
    var category by rememberSaveable { mutableStateOf("Потребности") }
    var selected by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var modeling by rememberSaveable { mutableStateOf(false) }
    val chosen = selected.mapNotNull { id -> words.find { it.id == id } }
    val phrase = Language.phrase(chosen)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading("Я хочу сказать", if (modeling) "Взрослый показывает пример. Повторять необязательно." else "Нажимай на картинки")
        Surface(color = Mint, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(phrase.ifEmpty { "Твоя фраза появится здесь" }, fontSize = if (s.large) 32.sp else 26.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Action("Сказать", enabled = chosen.isNotEmpty()) {
                        speak(phrase)
                        record(Attempt("communication", selected.last(), "COMMUNICATION", LocalDate.now(), true, 0, 0, modeled = modeling))
                    }
                    Secondary("Убрать слово", enabled = chosen.isNotEmpty()) { selected = selected.dropLast(1) }
                    Secondary("Очистить", enabled = chosen.isNotEmpty()) { selected = emptyList() }
                }
            }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Потребности", "Еда", "Напитки", "Игрушки", "Животные", "Действия").forEach { c ->
                FilterChip(category == c, { category = c }, label = { Text(c, fontSize = 18.sp, modifier = Modifier.padding(vertical = 12.dp)) })
            }
        }
        val core = listOf("want", "give", "more", "no", "yes", "help")
        val visible = words.filter { it.communication && it.id in core } + words.filter { it.communication && it.category == category && it.id !in core }
        LazyVerticalGrid(columns = GridCells.Adaptive(if (s.large) 188.dp else 148.dp), modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visible, key = { it.id }) { w ->
                Card(onClick = {
                    if (selected.size < 10) selected = selected + w.id
                    speak(w.spoken)
                }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Illustration(w.image, w.text, Modifier.size(if (s.large) 110.dp else 86.dp))
                        Text(w.text, fontSize = if (s.large) 25.sp else 21.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (selected.size >= 10) Text("Можно сказать фразу или убрать слово.", fontSize = 18.sp)
        Secondary(if (modeling) "Завершить показ взрослого" else "Показать вместе со взрослым") { modeling = !modeling; selected = emptyList() }
    }
}
@Composable fun ExercisePicker(content: Content, history: List<Attempt>, s: Settings, learn: Boolean, open: (Exercise) -> Unit) {
    var typeName by rememberSaveable(learn) { mutableStateOf(if (learn) "EXPLORE" else "LETTER") }
    val types = if (learn) listOf(ExerciseType.EXPLORE, ExerciseType.BUILD) else listOf(ExerciseType.LETTER, ExerciseType.COPY, ExerciseType.PICTURE, ExerciseType.PHRASE)
    val allowed = content.exercises.filter { it.type in types && it.level <= s.maxLevel && it.type.name in s.allowed }
    val recommendation = AdaptiveLearning.select(allowed, history, LocalDate.now(), s)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Heading(if (learn) "Знакомые слова" else "Печатать", "Можно выбрать любое доступное задание")
        if (recommendation != null && s.adaptive) Action("Предложение: ${recommendation.text}") { open(recommendation) }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.filter { it.name in s.allowed }.forEach { type -> FilterChip(typeName == type.name, { typeName = type.name }, label = { Text(type.title, Modifier.padding(vertical = 12.dp), fontSize = 18.sp) }) }
        }
        val visible = allowed.filter { it.type.name == typeName }
        if (visible.isEmpty()) Info("Для этого режима сейчас нет заданий. Взрослый может изменить уровень или выбрать упражнения в настройках.")
        LazyVerticalGrid(GridCells.Adaptive(if (s.large) 210.dp else 175.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visible, key = { it.id }) { ex -> Card(onClick = { open(ex) }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Illustration(ex.image, ex.text, Modifier.size(if (s.large) 118.dp else 94.dp))
                    Text(ex.text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            } }
        }
    }
}
