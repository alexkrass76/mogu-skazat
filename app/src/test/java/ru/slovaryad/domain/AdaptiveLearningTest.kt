package ru.slovaryad.domain

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AdaptiveLearningTest {
    private val today = LocalDate.of(2026, 10, 31)
    private fun attempt(day: Int = 0, hints: Int = 0, errors: Int = 0, modeled: Boolean = false,
        completed: Boolean = true, success: Boolean = true, id: String = "cat") =
        Attempt(id, "cat", "BUILD", today.plusDays(day.toLong()), success, hints, errors, modeled, completed, day.toLong() * 1000)
    @Test fun `быстрые успехи в один день не уменьшают помощь`() {
        assertEquals(1, AdaptiveLearning.recommend(List(10) { attempt() }).support)
    }
    @Test fun `самостоятельность подтверждается в разные дни`() {
        val result = AdaptiveLearning.recommend(listOf(attempt(-3), attempt(-2), attempt(-1), attempt()))
        assertEquals(0, result.support)
        assertEquals(3, result.intervalDays)
    }
    @Test fun `образец и ограниченные буквы не дают самостоятельного ответа`() {
        assertFalse(attempt(hints = 1).independent)
        assertFalse(attempt(errors = 1).independent)
        assertFalse(attempt(modeled = true).independent)
        assertFalse(attempt(completed = false).independent)
    }
    @Test fun `повторные несовпадения увеличивают предложенную помощь`() {
        assertEquals(2, AdaptiveLearning.recommend(listOf(attempt(-1, errors = 1), attempt(errors = 1))).support)
    }
    @Test fun `незавершённый ответ не становится освоенным`() {
        assertEquals(1, AdaptiveLearning.recommend(List(5) { attempt(it, completed = false) }).support)
    }
    @Test fun `показы взрослого не становятся освоением`() {
        assertFalse(AdaptiveLearning.mastered(List(5) { attempt(it, modeled = true) }))
    }
    @Test fun `подсказки препятствуют автоматическому уменьшению помощи`() {
        assertEquals(1, AdaptiveLearning.recommend(List(5) { attempt(it, hints = 1) }).support)
    }
    @Test fun `неуспешный завершённый ответ предлагает образец`() {
        assertEquals(2, AdaptiveLearning.recommend(listOf(attempt(success = false))).support)
    }
    private fun exercise(id: String, level: Int = 1, type: ExerciseType = ExerciseType.BUILD) =
        Exercise(id, type, "Порядок букв", level, "КОТ", "cat", "cat", "Собери", "Кот", listOf("Буква"), "exact_text")
    @Test fun `выбор уважает ограничения взрослого`() {
        val candidates = listOf(exercise("hard", 3), exercise("allowed"), exercise("disabled", type = ExerciseType.PICTURE))
        val selected = AdaptiveLearning.select(candidates, emptyList(), today, Settings(maxLevel = 1, allowed = setOf("BUILD")))
        assertEquals("allowed", selected?.id)
    }
    @Test fun `пустой набор настроек не возвращает скрытое упражнение`() {
        assertNull(AdaptiveLearning.select(listOf(exercise("cat")), emptyList(), today, Settings(allowed = emptySet())))
    }
    @Test fun `давно не повторявшееся задание получает приоритет`() {
        val candidates = listOf(exercise("cat"), exercise("dog"))
        val selected = AdaptiveLearning.select(candidates, listOf(attempt(-5), attempt(0, id = "dog")), today, Settings())
        assertEquals("cat", selected?.id)
    }
    @Test fun `новое задание доступно без прохождения предыдущих`() {
        assertEquals("dog", AdaptiveLearning.select(listOf(exercise("cat"), exercise("dog")), listOf(attempt()), today, Settings())?.id)
    }
    @Test fun `ошибки перед выходом тоже позволяют предложить помощь`() {
        assertEquals(2, AdaptiveLearning.recommend(listOf(attempt(errors = 2, completed = false))).support)
        assertFalse(AdaptiveLearning.mastered(listOf(attempt(errors = 2, completed = false))))
    }

}
