package ru.slovaryad.domain

import org.junit.Assert.*
import org.junit.Test

class LanguageTest {
    private fun word(id: String, text: String, form: String = text) = Word(id, text, "Еда", "apple", text, form)
    @Test fun `фраза использует форму дополнения после хочу и дай`() {
        assertEquals("ХОЧУ ВОДУ", Language.phrase(listOf(word("want", "ХОЧУ"), word("water", "ВОДА", "ВОДУ"))))
        assertEquals("ДАЙ МЯЧ", Language.phrase(listOf(word("give", "ДАЙ"), word("ball", "МЯЧ"))))
        assertEquals("ЕЩЁ ИГРАТЬ", Language.phrase(listOf(word("more", "ЕЩЁ"), word("play", "ИГРАТЬ"))))
    }
    @Test fun `повторяющиеся буквы не исчезают из целевого слова`() {
        assertEquals(listOf("А", "М"), Language.letters("МАМА", false))
        assertTrue(Language.matches("мама", "МАМА"))
    }
    @Test fun `полный алфавит содержит ё и оба знака`() {
        val letters = Language.letters("КОТ", true)
        assertEquals(33, letters.size)
        assertTrue(letters.containsAll(listOf("Ё", "Ъ", "Ь")))
    }
    @Test fun `ё не заменяется на е при проверке`() { assertFalse(Language.matches("ЕЩЕ", "ЕЩЁ")) }
    @Test fun `пробелы и регистр не мешают проверке фразы`() { assertTrue(Language.matches("  хочу   воду ", "ХОЧУ ВОДУ")) }
    @Test fun `пустой ответ не совпадает с целевым`() { assertFalse(Language.matches("", "КОТ")) }
}
