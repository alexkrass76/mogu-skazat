package ru.slovaryad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.slovaryad.data.RussianSpeech
import ru.slovaryad.domain.*
import ru.slovaryad.presentation.*

class MainActivity : ComponentActivity() {
    private lateinit var speech: RussianSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speech = RussianSpeech(this)
        setContent { SlovaTheme { SlovaApp(speech) } }
    }
    override fun onStop() { speech.stop(); super.onStop() }
    override fun onDestroy() { speech.close(); super.onDestroy() }
}
@Composable fun SlovaApp(speech: RussianSpeech, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val voiceStatus by speech.status.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("home") }
    var exerciseId by rememberSaveable { mutableStateOf<String?>(null) }
    var gate by remember { mutableStateOf(false) }
    var adultAccess by remember { mutableStateOf(false) }
    LaunchedEffect(screen) {
        if (screen !in listOf("parent", "history")) adultAccess = false
        if (screen in listOf("parent", "history") && !adultAccess) screen = "home"
    }
    val content = state.content
    val s = state.settings
    val speak: (String) -> Unit = { speech.speak(it, s.sound, s.rate) }
    LaunchedEffect(s.sound) { if (!s.sound) speech.stop() }
    val exercise = content?.exercises?.find { it.id == exerciseId }
    BackHandler(exercise == null && screen != "home") { screen = "home" }
    Surface(Modifier.fillMaxSize(), color = Paper) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 12.dp)) {
            if (exercise == null) {
                Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (screen != "home") Secondary("На главную") { screen = "home" }
                    else Text("могу сказать", fontSize = 22.sp, color = Green, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    if (screen != "parent") Secondary("Взрослым") { gate = true }
                }
            }
            if (state.error != null) {
                Info(state.error!!)
                Secondary("Понятно") { vm.dismissError() }
            }
            if (!state.loaded || content == null) {
                Text("Открываем слова…", fontSize = 24.sp)
            } else if (exercise != null) {
                key(exercise.id) { PracticeScreen(exercise, s, state.history, speak, vm::record,
                    onExit = { exerciseId = null }, onCommunicate = { screen = "talk" }, onNext = {
                        val candidates = content.exercises.filter { it.type == exercise.type }
                        val next = AdaptiveLearning.select(candidates.filter { it.id != exercise.id }, state.history, java.time.LocalDate.now(), s)
                        exerciseId = next?.id
                    }) }
            } else when (screen) {
                "home" -> Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Heading(if (s.name.isBlank()) "Давай общаться" else "Привет, ${s.name}!", "Выбирай, что хочется сделать")
                    Spacer(Modifier.height(8.dp))
                    MenuTile("Общаться", "Показать, попросить, поделиться", "want", s.large) { screen = "talk" }
                    MenuTile("Учить слова", "Картинки, слова и буквы", "cat", s.large) { screen = "learn" }
                    MenuTile("Печатать", "Буква, слово или целая фраза", "home", s.large) { screen = "type" }
                    MenuTile("Мои достижения", "Слова, с которыми мы знакомились", "play", s.large) { screen = "achievements" }
                    if (!s.quiet) Text("Можно выбрать картинку или напечатать. Говорить вслух необязательно.", fontSize = 17.sp)
                }
                "talk" -> CommunicationScreen(content.words, s, speak, vm::record)
                "learn", "type" -> ExercisePicker(content, state.history, s, screen == "learn") { exerciseId = it.id }
                "achievements" -> ProgressScreen(content, state.history, false)
                "parent" -> if (adultAccess) ParentScreen(s, content, state.history, voiceStatus, state.deleting, vm::save,
                    { vm.erase { screen = "home"; exerciseId = null } }, { screen = "history" })
                "history" -> if (adultAccess) ProgressScreen(content, state.history, true)
            }
        }
    }
    if (gate) ParentGate(onDismiss = { gate = false }, onSuccess = { gate = false; adultAccess = true; screen = "parent" })
}
