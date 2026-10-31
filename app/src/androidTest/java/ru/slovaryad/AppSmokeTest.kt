package ru.slovaryad

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import ru.slovaryad.data.ContentLoader
import ru.slovaryad.domain.ExerciseType
import ru.slovaryad.presentation.illustrationResource

@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun homeReady() { rule.waitUntil(15000) { rule.onAllNodesWithText("Общаться").fetchSemanticsNodes().isNotEmpty() } }
    @Test fun communicationIsAvailableWithoutLessons() {
        homeReady()
        rule.onNodeWithText("Общаться").performClick()
        rule.onNodeWithText("ХОЧУ").performClick()
        rule.onNodeWithText("Напитки").performClick()
        rule.onNodeWithText("ВОДА").performClick()
        rule.onNodeWithText("ХОЧУ ВОДУ").assertIsDisplayed()
        rule.onNodeWithText("Сказать").performClick()
        rule.onNodeWithText("Очистить").performClick()
        rule.onNodeWithText("Твоя фраза появится здесь").assertIsDisplayed()
    }
    @Test fun letterKeyboardAndReturnToCommunication() {
        homeReady()
        rule.onNodeWithText("Печатать").performClick()
        rule.onNodeWithText("К", useUnmergedTree = false).performClick()
        rule.onNode(hasText("К") and hasClickAction()).performClick()
        rule.onNodeWithText("Готово").performScrollTo().performClick()
        rule.onNodeWithText("Получилось! Можно продолжить или отдохнуть.").assertIsDisplayed()
        rule.onNodeWithText("Общаться").performScrollTo().performClick()
        rule.onNodeWithText("Я хочу сказать").assertIsDisplayed()
    }
    @Test fun contentReferencesAreComplete() {
        val content = ContentLoader.load(rule.activity)
        assertEquals(50, content.words.size)
        assertEquals(216, content.exercises.size)
        assertEquals(22, content.words.count { it.communication })
        assertEquals(content.words.size, content.words.map { it.id }.distinct().size)
        assertEquals(content.exercises.size, content.exercises.map { it.id }.distinct().size)
        val alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ ".toSet()
        content.words.forEach { word ->
            assertTrue(word.learningGroup.isNotBlank())
            val resource = illustrationResource(word.image)
            assertNotNull("Нет иллюстрации: ${word.id}", resource)
            assertNotNull(rule.activity.getDrawable(resource!!))
            listOf(ExerciseType.EXPLORE, ExerciseType.BUILD, ExerciseType.COPY, ExerciseType.PICTURE).forEach { type ->
                assertTrue("Нет режима $type для ${word.id}", content.exercises.any { it.wordId == word.id && it.type == type && it.text == word.text })
            }
        }
        content.exercises.forEach { ex ->
            assertTrue(content.words.any { it.id == ex.wordId })
            assertTrue(ex.hints.isNotEmpty())
            assertTrue(ex.level in 1..3)
            assertTrue(ex.text.all { it in alphabet })
            assertNotNull("Нет иллюстрации упражнения ${ex.id}", illustrationResource(ex.image))
        }
    }
    @Test fun parentSettingsAreProtected() {
        homeReady()
        rule.onNodeWithText("Взрослым").performClick()
        rule.onNodeWithText("Для взрослого").assertIsDisplayed()
        rule.onNodeWithText("Открыть").performClick()
        rule.onNodeWithText("Проверьте ответ и попробуйте ещё раз.").assertIsDisplayed()
        rule.onNodeWithText("Удалить профиль и всю историю").assertDoesNotExist()
    }
    @Test fun repeatedLetterTilesAndRecreationKeepAttempt() {
        homeReady()
        rule.onNodeWithText("Учить слова").performClick()
        rule.onNodeWithText("Собрать слово").performClick()
        rule.onNodeWithText("Общение").performScrollTo().performClick()
        rule.onNodeWithText("ПОМОГИ").performClick()
        rule.onNode(hasText("П") and hasClickAction() and isEnabled()).performClick()
        rule.activityRule.scenario.recreate()
        rule.onNode(hasText("П") and !hasClickAction()).assertExists()
        for (letter in listOf("О", "М", "О", "Г", "И")) {
            rule.onAllNodes(hasText(letter) and hasClickAction() and isEnabled())[0].performClick()
        }
        rule.onNodeWithText("Готово").performScrollTo().performClick()
        rule.onNodeWithText("Получилось! Можно продолжить или отдохнуть.").assertIsDisplayed()
    }
    @Test fun newWordsCanBeFilteredAndTypedWithYo() {
        homeReady()
        rule.onNodeWithText("Печатать").performClick()
        rule.onNodeWithText("По образцу").performClick()
        rule.onNodeWithText("Животные").performScrollTo().performClick()
        rule.onNodeWithText("СУП").assertDoesNotExist()
        rule.onNodeWithText("Одна буква").performClick()
        rule.onNodeWithTag("exercise_groups").assertDoesNotExist()
        rule.onNodeWithText("По образцу").performClick()
        rule.onNodeWithTag("exercise_grid").performScrollToNode(hasText("ЁЖ"))
        rule.onNodeWithText("ЁЖ").performClick()
        rule.onNode(hasText("Ё") and hasClickAction()).performClick()
        rule.onNode(hasText("Ж") and hasClickAction()).performClick()
        rule.onNodeWithText("Готово").performScrollTo().performClick()
        rule.onNodeWithText("Получилось! Можно продолжить или отдохнуть.").assertIsDisplayed()
    }
    private fun openParent() {
        rule.onNodeWithText("Взрослым").performClick()
        val question = rule.onNode(hasText("Чтобы открыть настройки", substring = true))
            .fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
        val numbers = Regex("[0-9]+").findAll(question).map { it.value.toInt() }.toList()
        rule.onNode(hasSetTextAction()).performTextInput(numbers.sum().toString())
        rule.onNodeWithText("Открыть").performClick()
    }
    @Test fun parentCanSaveAndDeleteProfileThroughUi() {
        homeReady()
        openParent()
        rule.onNode(hasSetTextAction()).performTextReplacement("Проверка профиля")
        rule.onNodeWithText("Сохранить имя").performScrollTo().performClick()
        rule.onNodeWithText("На главную").performClick()
        rule.onNodeWithText("Привет, Проверка профиля!").assertIsDisplayed()
        openParent()
        rule.onNodeWithText("Удалить профиль и всю историю").performScrollTo().performClick()
        rule.onNodeWithText("Удалить всё").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithText("Давай общаться").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Давай общаться").assertIsDisplayed()
    }

}
