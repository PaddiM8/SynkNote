package org.synknote.preferences

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceGroup
import android.util.Log

class PrefManager(context: Context, prefGroup: PrefGroup) {
    private val _context = context
    private val _prefGroup = prefGroup

    fun setString(name: String, value: String): PrefManager {
        get().edit().putString(name, value).apply()
        return this
    }

    fun setBoolean(name: String, value: Boolean): PrefManager {
        get().edit().putBoolean(name, value).apply()
        return this
    }

    fun getString(name: String): String {
        return if (get().contains(name))
            get().getString(name, null)
        else ""
    }

    fun getBoolean(name: String): Boolean {
        return if (get().contains(name))
            get().getBoolean(name, false)
        else false
    }

    fun contains(key: String): Boolean {
        return get().contains(key)
    }

    fun get(): SharedPreferences {
        return _context.getSharedPreferences(_prefGroup.name, AppCompatActivity.MODE_PRIVATE)
    }
}