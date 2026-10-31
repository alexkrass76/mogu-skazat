package ru.slovaryad.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class RussianSpeech(context: Context) {
    private val mutableStatus = MutableStateFlow("Проверяем русский голос…")
    val status = mutableStatus.asStateFlow()
    private var ready = false
    private var closed = false
    private var tts: TextToSpeech? = null
    init { tts = TextToSpeech(context.applicationContext) { result ->
        if (!closed) {
            val engine = tts
            if (result == TextToSpeech.SUCCESS && engine != null) {
                val voice = engine.voices?.filter {
                    it.locale.language == "ru" && !it.isNetworkConnectionRequired &&
                        "notInstalled" !in it.features.orEmpty()
                }?.sortedBy { it.name }?.firstOrNull()
                ready = voice != null && engine.setVoice(voice) == TextToSpeech.SUCCESS
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {}
                    @Deprecated("Системный обратный вызов")
                    override fun onError(id: String?) { mutableStatus.value = "Не удалось озвучить. Проверьте русский голос в настройках устройства." }
                })
            }
            mutableStatus.value = if (ready) "Русский голос готов. Озвучивание работает без сети."
                else "Нет доступного русского голоса без сети. Взрослый может установить его в настройках синтеза речи устройства. Все задания доступны без озвучивания."
        }
    } }
    fun speak(text: String, enabled: Boolean, rate: Float) {
        if (!enabled || !ready || closed) return
        tts?.setSpeechRate(rate)
        if (tts?.speak(text.lowercase(Locale.forLanguageTag("ru")), TextToSpeech.QUEUE_FLUSH, null, "slova-${System.nanoTime()}") == TextToSpeech.ERROR)
            mutableStatus.value = "Озвучивание недоступно. Проверьте голос в настройках устройства."
    }
    fun stop() { tts?.stop() }
    fun close() { closed = true; ready = false; tts?.stop(); tts?.shutdown(); tts = null }
}
