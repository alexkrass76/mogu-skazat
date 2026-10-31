package ru.slovaryad.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import ru.slovaryad.domain.*
import java.time.LocalDate

private val Context.preferences by preferencesDataStore("family")
class SettingsStore(private val context: Context) {
    private val name = stringPreferencesKey("name")
    private val sound = booleanPreferencesKey("sound")
    private val large = booleanPreferencesKey("large")
    private val quiet = booleanPreferencesKey("quiet")
    private val rate = floatPreferencesKey("rate")
    private val hints = booleanPreferencesKey("hints")
    private val full = booleanPreferencesKey("full")
    private val highlight = booleanPreferencesKey("highlight")
    private val level = intPreferencesKey("level")
    private val adaptive = booleanPreferencesKey("adaptive")
    private val allowed = stringSetPreferencesKey("allowed")
    val settings: Flow<Settings> = context.preferences.data.map { p -> Settings(
        p[name] ?: "", p[sound] ?: true, p[large] ?: false, p[quiet] ?: false,
        p[rate] ?: .85f, p[hints] ?: true, p[full] ?: false, p[highlight] ?: false,
        p[level] ?: 2, p[adaptive] ?: true, p[allowed] ?: ExerciseType.entries.map { it.name }.toSet()) }
    suspend fun save(s: Settings) { context.preferences.edit { p ->
        p[name] = s.name.take(40); p[sound] = s.sound; p[large] = s.large; p[quiet] = s.quiet
        p[rate] = s.rate.coerceIn(.5f, 1.2f); p[hints] = s.hints; p[full] = s.fullKeyboard
        p[highlight] = s.highlight; p[level] = s.maxLevel.coerceIn(1, 3); p[adaptive] = s.adaptive; p[allowed] = s.allowed
    } }
    suspend fun clear() { context.preferences.edit { it.clear() } }
}
@Entity(tableName = "attempts")
data class AttemptEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: String, val wordId: String, val type: String, val day: String,
    val success: Boolean, val hints: Int, val errors: Int, val modeled: Boolean,
    val completed: Boolean, val timestamp: Long) {
    fun domain() = Attempt(exerciseId, wordId, type, LocalDate.parse(day), success, hints, errors, modeled, completed, timestamp)
    companion object { fun from(a: Attempt) = AttemptEntity(0, a.exerciseId, a.wordId, a.type, a.day.toString(), a.success, a.hints, a.errors, a.modeled, a.completed, a.timestamp) }
}
@Dao interface AttemptDao {
    @Query("SELECT * FROM attempts ORDER BY timestamp DESC, id DESC") fun observe(): Flow<List<AttemptEntity>>
    @Insert suspend fun insert(attempt: AttemptEntity)
    @Query("DELETE FROM attempts") suspend fun deleteAll()
}
@Database(entities = [AttemptEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() { abstract fun attempts(): AttemptDao }
class Repository(context: Context) {
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "slova.db").build()
    val settings = SettingsStore(context)
    val history = db.attempts().observe().map { items -> items.map { it.domain() } }
    suspend fun record(a: Attempt) = db.attempts().insert(AttemptEntity.from(a))
    suspend fun erase() { db.attempts().deleteAll(); settings.clear() }
    fun close() { db.close() }
}
object ContentLoader {
    fun load(context: Context): Content {
        val root = JSONObject(context.assets.open("content.json").bufferedReader().use { it.readText() })
        val words = root.getJSONArray("words").let { list -> (0 until list.length()).map { i ->
            val o = list.getJSONObject(i)
            Word(o.getString("id"), o.getString("text"), o.getString("category"), o.getString("image"),
                o.getString("spoken"), o.getString("phraseForm"), o.optBoolean("communication", true),
                o.optString("learningGroup", o.getString("category")))
        } }
        val exercises = root.getJSONArray("exercises").let { list -> (0 until list.length()).map { i ->
            val o = list.getJSONObject(i)
            Exercise(o.getString("id"), ExerciseType.valueOf(o.getString("type")), o.getString("skill"), o.getInt("level"), o.getString("text"), o.getString("wordId"), o.getString("image"), o.getString("instruction"), o.getString("speech"), o.getJSONArray("hints").let { h -> (0 until h.length()).map { h.getString(it) } }, o.getString("completion"))
        } }
        require(words.map { it.id }.distinct().size == words.size)
        require(words.all { it.text.isNotBlank() && it.learningGroup.isNotBlank() })
        require(exercises.map { it.id }.distinct().size == exercises.size)
        require(exercises.all { ex -> words.any { it.id == ex.wordId } && ex.text.isNotBlank() && ex.hints.isNotEmpty() })
        return Content(words, exercises)
    }
}
