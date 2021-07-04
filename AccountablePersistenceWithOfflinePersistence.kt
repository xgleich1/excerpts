import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.core.content.edit
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

/**
 * Wrapper around the shared preferences
 * designed to achieve the following:
 *
 * You only need to look at one place to see what
 * is saved locally on the users device and with
 * which key. For this the key is strongly typed.
 * You only have to look at the subclasses of the
 * OfflinePersistenceKey to answer the questions.
 *
 * The underlying persistence method is a detail.
 * It should be easy to change. This is achieved
 * by never exposing anything from the shared
 * preferences in the api surface of the class.
 *
 * It should be easy to swap the underlying
 * persistence method in ui-tests. Providing
 * a different builder will do exactly that.
 */

class OfflinePersistence(
        sharedPreferencesBuilder: SharedPreferencesBuilder) {

    private val sharedPreferences = sharedPreferencesBuilder.build()


    fun saveBoolean(key: OfflinePersistenceKey, value: Boolean) = sharedPreferences
            .commit { putBoolean(key.rawKey, value) }

    fun loadBoolean(key: OfflinePersistenceKey, default: Boolean) = sharedPreferences
            .getBoolean(key.rawKey, default)

    fun saveInteger(key: OfflinePersistenceKey, value: Int) = sharedPreferences
            .commit { putInt(key.rawKey, value) }

    fun loadInteger(key: OfflinePersistenceKey, default: Int) = sharedPreferences
            .getInt(key.rawKey, default)

    fun deleteAllValues() = sharedPreferences.commit { clear() }

    private fun SharedPreferences.commit(action: Editor.() -> Unit) =
            edit(commit = true, action = action)
}

abstract class OfflinePersistenceKey(val rawKey: String) {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as OfflinePersistenceKey

        if(rawKey != other.rawKey) return false

        return true
    }

    override fun hashCode() = rawKey.hashCode()

    override fun toString() = "OfflinePersistenceKey($rawKey)"
}

/**
 * Strongly typing the key makes it
 * easy to find in the class search.
 */
object SomeOfflinePersistenceKey
    : OfflinePersistenceKey("some_offline_persistence_key")

/**
 * The interface makes it easy to provide
 * different shared preferences in ui tests.
 */
interface SharedPreferencesBuilder {
    fun build(): SharedPreferences
}

class SharedPreferencesBuilderImpl(
        private val application: Application
) : SharedPreferencesBuilder {

    override fun build(): SharedPreferences = application
            .getSharedPreferences("shared_preferences_v1", MODE_PRIVATE)
}

class TestSharedPreferencesBuilder : SharedPreferencesBuilder {
    private val context get() = getInstrumentation().targetContext


    override fun build(): SharedPreferences = context
            .getSharedPreferences("shared_preferences_test", MODE_PRIVATE)
}