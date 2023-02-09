package ru.ivos.plumbing_test.data

import android.content.Context
import android.preference.PreferenceManager
import ru.ivos.plumbing_test.utils.Utils

class PreferencesRepo(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getStartLatitude() : String? = prefs.getString(Utils.START_LATITUDE_PREF, "")
    fun getStartLongitude() : String? = prefs.getString(Utils.START_LONGITUDE_PREF, "")


    fun setStartLatitude(latitude: String) {
        prefs.edit().putString(Utils.START_LATITUDE_PREF, latitude).apply()
    }
    fun setStartLongitude(longitude: String) {
        prefs.edit().putString(Utils.START_LONGITUDE_PREF, longitude).apply()
    }
}