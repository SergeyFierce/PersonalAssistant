package ru.topskiy.personalassistant.core.datastore

import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory SharedPreferences для unit-тестов на JVM (без Android Context).
 * Реализует только методы, необходимые для теста миграции.
 */
class InMemorySharedPreferences : SharedPreferences {

    private val map = ConcurrentHashMap<String, Any?>()

    override fun getAll(): MutableMap<String, *> = HashMap(map)

    override fun getString(key: String, defValue: String?): String? =
        (map[key] as? String) ?: defValue

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        val value = map[key]
        return when (value) {
            is Set<*> -> value.mapNotNull { it as? String }.toMutableSet()
            else -> defValues
        }
    }

    override fun getInt(key: String, defValue: Int): Int = (map[key] as? Int) ?: defValue
    override fun getLong(key: String, defValue: Long): Long = (map[key] as? Long) ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = (map[key] as? Float) ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        (map[key] as? Boolean) ?: defValue

    override fun contains(key: String): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = Editor(map)
    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) = Unit

    private class Editor(private val map: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor {
            pending[key] = values
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            pending[key] = null
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            pending.clear()
            pending.putAll(map.keys.associateWith { null })
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            pending.forEach { (k, v) ->
                if (v == null) map.remove(k) else map[k] = v
            }
            pending.clear()
        }
    }
}
