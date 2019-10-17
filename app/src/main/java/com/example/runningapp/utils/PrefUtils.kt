package com.example.runningapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.runningapp.R

class PrefUtils (val context: Context) {

    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveString(KEY_NAME: String, text: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME, text)

        editor.commit()
    }

    fun saveInt(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putInt(KEY_NAME, value)

        editor.apply()
    }

    fun saveBoolean(KEY_NAME: String, status: Boolean) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putBoolean(KEY_NAME, status)

        editor.commit()
    }

    fun getValueString(KEY_NAME: String): String? {

        return sharedPref.getString(KEY_NAME, null)


    }

    fun getValueInt(KEY_NAME: String): Int {

        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {

        return sharedPref.getBoolean(KEY_NAME, defaultValue)

    }

    fun clearSharedPreference() {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        editor.clear()
        editor.commit()
    }

    fun removeValue(KEY_NAME: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.remove(KEY_NAME)
        editor.commit()
    }

    fun getSavedTheme(): String {
        return context.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .getString(PREFERENCE_KEY_THEME, context.getString(R.string.defaultTheme))!!
    }

    fun saveTheme(selectedTheme: String) {
        context.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(PREFERENCE_KEY_THEME, selectedTheme)
            .apply()
    }
}
