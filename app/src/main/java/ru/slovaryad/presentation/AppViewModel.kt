package ru.slovaryad.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.slovaryad.data.*
import ru.slovaryad.domain.*

data class AppState(val content: Content? = null, val settings: Settings = Settings(),
    val history: List<Attempt> = emptyList(), val loaded: Boolean = false, val error: String? = null, val deleting: Boolean = false)
class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    private val content = MutableStateFlow<Content?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val deleting = MutableStateFlow(false)
    private val mutex = Mutex()
    private val settings = repository.settings.settings.catch { error.value = "Не удалось прочитать настройки. Закройте и откройте приложение."; emit(Settings()) }
    private val history = repository.history.catch { error.value = "Не удалось прочитать историю занятий."; emit(emptyList()) }
    val state = combine(content, settings, history, error, deleting) { c, s, h, e, d -> AppState(c, s, h, c != null, e, d) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppState())
    init { viewModelScope.launch {
        try { content.value = withContext(Dispatchers.IO) { ContentLoader.load(application) } }
        catch (_: Exception) { error.value = "Не удалось открыть упражнения. Попробуйте переустановить приложение после сохранения нужных результатов." }
    } }
    fun save(settings: Settings) { viewModelScope.launch { mutex.withLock {
        try { repository.settings.save(settings) } catch (_: Exception) { error.value = "Настройки не сохранены. Попробуйте ещё раз." }
    } } }
    fun record(attempt: Attempt) { viewModelScope.launch { mutex.withLock {
        try { repository.record(attempt) } catch (_: Exception) { error.value = "Результат не сохранён. На устройстве может не хватать места." }
    } } }
    fun erase(onDone: () -> Unit) { viewModelScope.launch { mutex.withLock {
        deleting.value = true
        try { repository.erase(); onDone() } catch (_: Exception) { error.value = "Удаление не завершено. Повторите попытку." }
        finally { deleting.value = false }
    } } }
    override fun onCleared() { repository.close(); super.onCleared() }
    fun dismissError() { error.value = null }
}
