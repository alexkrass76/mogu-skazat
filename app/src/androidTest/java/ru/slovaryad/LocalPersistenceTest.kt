package ru.slovaryad

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import ru.slovaryad.data.Repository
import ru.slovaryad.domain.*
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class LocalPersistenceTest {
    @Test fun historyAndProfileSurviveReopenAndCanBeErased() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val first = Repository(context)
        first.erase()
        first.settings.save(Settings(name = "Проверка", sound = false, large = true, allowed = setOf("BUILD")))
        first.record(Attempt("build_cat", "cat", "BUILD", LocalDate.of(2026, 10, 31), true, 0, 0))
        first.close()
        val reopened = Repository(context)
        try {
            assertEquals("Проверка", reopened.settings.settings.first().name)
            assertFalse(reopened.settings.settings.first().sound)
            assertEquals(1, reopened.history.first().size)
            assertTrue(reopened.history.first().single().independent)
            reopened.erase()
            assertTrue(reopened.history.first().isEmpty())
            assertEquals(Settings(), reopened.settings.settings.first())
        } finally { reopened.close() }
    }
}
