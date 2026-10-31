package ru.slovaryad.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.slovaryad.domain.*
import java.time.format.DateTimeFormatter

@Composable fun ParentGate(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val a = remember { (23..49).random() }
    val b = remember { (17..38).random() }
    var answer by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Для взрослого") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Чтобы открыть настройки, решите: $a + $b = ?")
            OutlinedTextField(answer, { answer = it.filter(Char::isDigit).take(3); wrong = false },
                label = { Text("Ответ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            if (wrong) Text("Проверьте ответ и попробуйте ещё раз.")
        }
    }, confirmButton = { TextButton({ if (answer.toIntOrNull() == a + b) onSuccess() else wrong = true }) { Text("Открыть") } },
        dismissButton = { TextButton(onDismiss) { Text("Назад") } })
}
@Composable private fun SettingToggle(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f).padding(end = 16.dp), fontSize = 19.sp)
        Switch(checked, onChange)
    }
}
@Composable fun ParentScreen(s: Settings, content: Content, history: List<Attempt>, voiceStatus: String,
    deleting: Boolean, save: (Settings) -> Unit, erase: () -> Unit, openHistory: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(s.name) }
    var deleteDialog by remember { mutableStateOf(false) }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Heading("Рядом со взрослым", "Настройте занятия под интересы ребёнка")
        Info("Это образовательный помощник. Он не ставит диагноз и не заменяет занятия со специалистом. Можно общаться без устного ответа.")
        Text("Профиль на этом устройстве", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(name, { name = it.take(40) }, label = { Text("Имя или псевдоним — необязательно") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Secondary("Сохранить имя") { save(s.copy(name = name.trim())) }
        Text("Комфорт", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        SettingToggle("Озвучивать слова", s.sound) { save(s.copy(sound = it)) }
        Info(voiceStatus)
        Text("Скорость озвучивания: ${java.lang.String.format(java.util.Locale.forLanguageTag("ru"), "%.2f", s.rate)}", fontSize = 19.sp)
        Slider(s.rate, { save(s.copy(rate = it)) }, valueRange = .5f..1.2f, steps = 6)
        SettingToggle("Крупные карточки и буквы", s.large) { save(s.copy(large = it)) }
        SettingToggle("Меньше поясняющих элементов", s.quiet) { save(s.copy(quiet = it)) }
        Text("Движущихся анимаций, таймеров и звуковых наград в приложении нет.", fontSize = 17.sp)
        Text("Помощь и упражнения", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        SettingToggle("Доступны подсказки", s.hints) { save(s.copy(hints = it)) }
        SettingToggle("Полный русский алфавит", s.fullKeyboard) { save(s.copy(fullKeyboard = it)) }
        SettingToggle("Подсвечивать следующую букву", s.highlight) { save(s.copy(highlight = it)) }
        SettingToggle("Предлагать повторение и помощь", s.adaptive) { save(s.copy(adaptive = it)) }
        Text("Ограниченный набор букв и показ образца считаются помощью. Выключение подсказок не скрывает образец в заданиях «По образцу», «Одна буква» и «Короткая фраза».", fontSize = 17.sp)
        Text("Максимальный уровень: ${s.maxLevel}", fontSize = 19.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..3).forEach { level -> FilterChip(s.maxLevel == level, { save(s.copy(maxLevel = level)) }, label = { Text("Уровень $level", Modifier.padding(vertical = 12.dp)) }) }
        }
        Text("1 — знакомство и буква. 2 — сборка и набор по образцу. 3 — картинка и фраза. Уровень повышает только взрослый.", fontSize = 17.sp)
        ExerciseType.entries.forEach { type -> SettingToggle(type.title, type.name in s.allowed) { enabled ->
            save(s.copy(allowed = if (enabled) s.allowed + type.name else s.allowed - type.name))
        } }
        Info("Покажите на карточках, например, «ХОЧУ ВОДУ», затем предложите воду. Дайте время на любой ответ: взгляд, жест, картинку или слово. «Нет» и просьба об отдыхе доступны всегда.")
        Secondary("История и наблюдения (${history.size})") { openHistory() }
        val mastered = content.exercises.filter { AdaptiveLearning.mastered(history.filter { a -> a.exerciseId == it.id }) }
        Text("Устойчивые самостоятельные ответы: ${mastered.map { it.text }.distinct().joinToString().ifEmpty { "пока недостаточно наблюдений в разные дни" }}.", fontSize = 18.sp)
        Text("Все данные хранятся на устройстве. Приложение не использует микрофон, рекламу, аналитику или сеть. Автоматическое резервное копирование отключено.", fontSize = 17.sp)
        Secondary(if (deleting) "Удаляем…" else "Удалить профиль и всю историю", enabled = !deleting) { deleteDialog = true }
        Spacer(Modifier.height(24.dp))
    }
    if (deleteDialog) AlertDialog(onDismissRequest = { deleteDialog = false }, title = { Text("Удалить все данные?") },
        text = { Text("Имя, настройки и история занятий будут удалены с этого устройства. Восстановить их в приложении нельзя.") },
        confirmButton = { TextButton({ deleteDialog = false; erase() }, enabled = !deleting) { Text("Удалить всё") } },
        dismissButton = { TextButton({ deleteDialog = false }) { Text("Отмена") } })
}
@Composable fun ProgressScreen(content: Content, history: List<Attempt>, detailed: Boolean) {
    val learning = history.filter { it.type !in listOf("COMMUNICATION", "EXPLORE") && !it.modeled }
    val completed = learning.filter { it.completed }
    val familiar = history.map { it.wordId }.distinct().mapNotNull { id -> content.words.find { it.id == id } }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Heading(if (detailed) "Наблюдения взрослого" else "Мои достижения", "То, что мы делали в приложении")
        Info(if (history.isEmpty()) "Здесь появятся слова и занятия. Можно начать с любой карточки." else "Мы знакомились со словами: ${familiar.joinToString { it.text }}")
        Text("Завершённых заданий: ${completed.size}", fontSize = 22.sp)
        Text("Самостоятельных ответов: ${completed.count { it.independent }}", fontSize = 22.sp)
        Text("Составлено сообщений: ${history.count { it.type == "COMMUNICATION" && !it.modeled }}", fontSize = 22.sp)
        if (detailed) {
            val supported = completed.count { it.hints > 0 }
            Text("Завершено с помощью: $supported из ${completed.size}. Доля: ${if (completed.isEmpty()) 0 else supported * 100 / completed.size}%.", fontSize = 19.sp)
            Text("Отмечено проверок с несовпадением: ${learning.sumOf { it.errors }}. Незавершённых попыток: ${learning.count { !it.completed }}.", fontSize = 19.sp)
            Info("Самостоятельный ответ: правильный результат без образца, ограниченной клавиатуры, подсказок и неверных проверок в этой попытке. Это наблюдение в задании, а не оценка речи или способностей. Показ взрослого и знакомство со словом в этот счёт не входят.")
            Heading("Последние занятия")
            history.take(100).forEach { a ->
                val ex = content.exercises.find { it.id == a.exerciseId }
                val word = content.words.find { it.id == a.wordId }
                Surface(color = androidx.compose.ui.graphics.Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${a.day.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} · ${ex?.type?.title ?: if (a.modeled) "Показ взрослого" else "Сообщение"}", fontSize = 17.sp)
                        Text(ex?.text ?: word?.text.orEmpty(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(when { a.modeled -> "Показ или знакомство"; !a.completed -> "Остановились до завершения"; a.independent -> "Без помощи"; else -> "С помощью или после повторной попытки" }, fontSize = 17.sp)
                        if (a.type != "COMMUNICATION") Text("Отметки помощи: ${a.hints} · Проверки с несовпадением: ${a.errors}", fontSize = 16.sp)
                    }
                }
            }
            if (history.size > 100) Text("Показаны последние 100 записей. Итоги учитывают всю историю.")
        }
        Spacer(Modifier.height(20.dp))
    }
}
