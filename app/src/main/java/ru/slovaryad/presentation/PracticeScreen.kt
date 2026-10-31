@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package ru.slovaryad.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlin.random.Random

@Composable fun PracticeScreen(ex: Exercise, s: Settings, history: List<Attempt>, speak: (String) -> Unit,
    record: (Attempt) -> Unit, onExit: () -> Unit, onCommunicate: () -> Unit, onNext: () -> Unit) {
    val build = ex.type == ExerciseType.BUILD
    val explore = ex.type == ExerciseType.EXPLORE
    val hasModel = ex.type in listOf(ExerciseType.COPY, ExerciseType.PHRASE, ExerciseType.LETTER)
    // Настройки фиксируются на попытку. Изменение истории не меняет помощь во время ответа.
    val recommended = rememberSaveable(ex.id) { if (s.adaptive) AdaptiveLearning.recommend(history.filter { it.exerciseId == ex.id }).support else 1 }
    val fullKeyboard = rememberSaveable(ex.id) { s.fullKeyboard }
    var hintLevel by rememberSaveable(ex.id) { mutableIntStateOf(if (hasModel) 2 else if (s.hints && recommended == 2) 1 else 0) }
    val initialSupport = if (hasModel) 2 else if (!build && !explore && !fullKeyboard) 1 else if (s.hints && s.highlight && recommended > 0) 1 else 0
    var hints by rememberSaveable(ex.id) { mutableIntStateOf(maxOf(initialSupport, hintLevel)) }
    var errors by rememberSaveable(ex.id) { mutableIntStateOf(0) }
    var input by rememberSaveable(ex.id) { mutableStateOf("") }
    var used by rememberSaveable(ex.id) { mutableStateOf<List<Int>>(emptyList()) }
    var finished by rememberSaveable(ex.id) { mutableStateOf(false) }
    var message by rememberSaveable(ex.id) { mutableStateOf("") }
    var touched by rememberSaveable(ex.id) { mutableStateOf(false) }
    val choices = remember(ex.id) { ex.text.filter { it != ' ' }.map { it.toString() }.shuffled(Random(ex.id.hashCode())) }
    val leave = {
        if (!finished && touched && !explore) record(Attempt(ex.id, ex.wordId, ex.type.name, LocalDate.now(), false, hints, errors, completed = false))
        onExit()
    }
    BackHandler { leave() }
    val nextChar = ex.text.getOrNull(input.length)?.toString()
    val highlight = s.hints && !finished && (hintLevel > 0 || (s.highlight && recommended > 0))
    fun add(char: String, index: Int? = null) {
        if (!finished && input.length < ex.text.length + 8) {
            touched = true; input += char; message = ""
            if (index != null) used = used + index
            if (highlight) hints = maxOf(hints, 1)
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Secondary("К выбору слов") { leave() }
            Spacer(Modifier.weight(1f))
            Secondary("Общаться") { leave(); onCommunicate() }
        }
        Heading(ex.type.title, ex.instruction)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Illustration(ex.image, if (ex.type == ExerciseType.PICTURE) "Картинка для задания" else ex.speech,
                Modifier.size(if (s.large) 200.dp else 150.dp))
        }
        if (explore || hasModel || hintLevel >= 2) {
            Text(ex.text, Modifier.align(Alignment.CenterHorizontally), fontSize = if (s.large) 48.sp else 40.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Secondary(if (explore) "Послушать слово" else "Повторить инструкцию") { speak(if (explore) ex.speech else ex.instruction) }
        }
        if (explore) {
            if (!s.quiet) Info("Взрослый может показать предмет рядом или разыграть действие. Повторять слово необязательно.")
            Action("Посмотрели. Дальше", Modifier.align(Alignment.CenterHorizontally)) {
                if (!finished) { finished = true; record(Attempt(ex.id, ex.wordId, ex.type.name, LocalDate.now(), true, 1, 0, modeled = true)) }
                onNext()
            }
        } else {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(2.dp, if (finished) Green else Mint)) {
                Text(input.ifEmpty { "Нажимай на буквы" }, Modifier.padding(22.dp), fontSize = if (s.large) 38.sp else 32.sp, fontWeight = FontWeight.Bold)
            }
            if (hintLevel == 1 && nextChar != null && !finished) Info(if (nextChar == " ") "Теперь добавь пробел" else "Следующая буква: $nextChar")
            if (message.isNotEmpty()) Info(message)
            if (!finished) {
                if (build) {
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        choices.forEachIndexed { index, letter ->
                            LetterKey(letter, s.large, highlight && letter == nextChar, index !in used) { add(letter, index) }
                        }
                    }
                } else {
                    val letters = Language.letters(ex.text, fullKeyboard)
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        letters.forEach { letter -> LetterKey(letter, s.large, highlight && letter == nextChar, true) { add(letter) } }
                    }
                    if (' ' in ex.text) Secondary("Пробел", Modifier.align(Alignment.CenterHorizontally)) { add(" ") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    Secondary("Стереть", enabled = input.isNotEmpty()) {
                        input = input.dropLast(1); if (build) used = used.dropLast(1); message = ""
                    }
                    if (s.hints) Secondary("Подсказка") {
                        touched = true; hintLevel = (hintLevel + 1).coerceAtMost(2); hints = maxOf(hints + 1, hintLevel)
                    }
                    Action("Готово", enabled = input.isNotEmpty()) {
                        if (Language.matches(input, ex.text)) {
                            finished = true; speak(ex.speech); message = "Получилось! Можно продолжить или отдохнуть."
                            record(Attempt(ex.id, ex.wordId, ex.type.name, LocalDate.now(), true, hints, errors))
                        } else {
                            errors++; touched = true
                            message = if (s.hints && errors >= 2) "Попробуем ещё раз. Можно нажать «Подсказка»." else "Можно стереть букву и попробовать ещё раз."
                        }
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    Secondary("Послушать слово") { speak(ex.speech) }
                    Action("Дальше") { onNext() }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
@Composable private fun LetterKey(letter: String, large: Boolean, highlight: Boolean, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick, Modifier.padding(horizontal = 4.dp).size(if (large) 86.dp else 68.dp), enabled = enabled,
        shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (highlight) Peach else Color.White),
        border = BorderStroke(if (highlight) 3.dp else 1.dp, if (highlight) Green else Mint)) {
        Text(letter, fontSize = if (large) 36.sp else 30.sp, fontWeight = FontWeight.Bold)
    }
}
