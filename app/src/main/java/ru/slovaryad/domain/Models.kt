package ru.slovaryad.domain

import java.time.LocalDate

enum class ExerciseType(val title: String) {
    EXPLORE("Знакомство"), BUILD("Собрать слово"), LETTER("Одна буква"), COPY("По образцу"), PICTURE("По картинке"), PHRASE("Короткая фраза")
}
data class Word(val id: String, val text: String, val category: String, val image: String, val spoken: String,
    val phraseForm: String, val communication: Boolean = true, val learningGroup: String = category)
data class Exercise(val id: String, val type: ExerciseType, val skill: String, val level: Int, val text: String,
                    val wordId: String, val image: String, val instruction: String, val speech: String,
                    val hints: List<String>, val completion: String)
data class Content(val words: List<Word>, val exercises: List<Exercise>)
data class Settings(val name: String = "", val sound: Boolean = true, val large: Boolean = false,
    val quiet: Boolean = false, val rate: Float = 0.85f, val hints: Boolean = true,
    val fullKeyboard: Boolean = false, val highlight: Boolean = false, val maxLevel: Int = 2,
    val adaptive: Boolean = true, val allowed: Set<String> = ExerciseType.entries.map { it.name }.toSet())
data class Attempt(val exerciseId: String, val wordId: String, val type: String, val day: LocalDate,
    val success: Boolean, val hints: Int, val errors: Int, val modeled: Boolean = false,
    val completed: Boolean = true, val timestamp: Long = System.currentTimeMillis()) {
    val independent: Boolean get() = completed && success && hints == 0 && errors == 0 && !modeled
}
data class Recommendation(val support: Int, val intervalDays: Long, val explanation: String)

object AdaptiveLearning {
    fun recommend(history: List<Attempt>): Recommendation {
        val recent = history.filter { it.completed && !it.modeled }.sortedBy { it.timestamp }.takeLast(5)
        val lastInteractions = history.filter { !it.modeled && (it.completed || it.errors > 0) }
            .sortedBy { it.timestamp }.takeLast(2)
        if (lastInteractions.sumOf { it.errors } >= 2 || lastInteractions.any { it.completed && !it.success })
            return Recommendation(2, 1, "В последних попытках понадобилась помощь. Предложим образец.")
        if (recent.isEmpty()) return Recommendation(1, 1, "Пока мало наблюдений: можно предложить подсказку.")
        val independent = recent.filter { it.independent }
        if (independent.size >= 4 && independent.map { it.day }.distinct().size >= 3)
            return Recommendation(0, 3, "Есть четыре самостоятельных ответа как минимум в три разных дня.")
        return Recommendation(1, 1, "Продолжим знакомое задание с доступной подсказкой.")
    }
    fun mastered(history: List<Attempt>) = recommend(history).support == 0
    fun select(exercises: List<Exercise>, history: List<Attempt>, today: LocalDate, settings: Settings): Exercise? {
        val candidates = exercises.filter { it.level <= settings.maxLevel && it.type.name in settings.allowed }
        return candidates.minWithOrNull(compareBy<Exercise> { ex ->
            val records = history.filter { it.exerciseId == ex.id && it.completed && !it.modeled }
            val last = records.maxByOrNull { it.timestamp }
            if (last == null) Long.MIN_VALUE else last.day.toEpochDay() + recommend(records).intervalDays - today.toEpochDay()
        }.thenBy { it.id })
    }
}
object Language {
    fun phrase(words: List<Word>): String = words.mapIndexed { index, word ->
        if (index > 0 && words[index - 1].id in setOf("want", "give")) word.phraseForm else word.text
    }.joinToString(" ")
    fun normalize(text: String) = text.trim().uppercase(java.util.Locale.forLanguageTag("ru")).replace(Regex("\\s+"), " ")
    fun matches(actual: String, expected: String) = normalize(actual) == normalize(expected)
    fun letters(target: String, full: Boolean): List<String> = if (full)
        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".map { it.toString() }
    else target.filter { it != ' ' }.toSet().sorted().map { it.toString() }
}
