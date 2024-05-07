package com.izontechnology.dcapp.base.common

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.izontechnology.dcapp.data.response.DownloadInfoResponse

@Suppress("UNCHECKED_CAST")
class SharedPrefs(private val context: Context) {
    companion object {
        private const val PREF = "AppConfig"
        private const val PREF_TOKEN = "user_token"
        private const val PREF_USER = "user_data"
        private const val IS_LOGIN = "is_login"
        private const val ZIP_EXTRACTED = "zip_extracted"
        private const val SIM_HEADER = "sim_header"
        private const val PREVIOUS_WIFI_SSID = "previous_wifi_ssid"
        private const val IS_KIOSK = "is_kiosk"
        private const val EXIT_KIOSK_CODE = "exit_kiosk_code"
        private const val IS_FIRST_LAUNCH = "is_first_launch"
        private const val IS_WIFI = "is_wifi"
        private const val IS_GOLF_APP_LAUNCH = "is_golf_app_launch"
        private const val NIGHT_UPDATE_HOUR = "NIGHT_UPDATE_HOUR"
        private const val NIGHT_UPDATE_MINUTE = "NIGHT_UPDATE_MINUTE"
        private const val REMEMBER_CODE = "remember_code"
        private const val THEME = "theme"
        private const val DOWNLOAD_ID = "downoad_id"
        private const val TILES_DATA = "tiles_data"
    }

    val sharedPref: SharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun getManualTheme(): Int {
        return get(THEME, Int::class.java)
    }

    fun setManualTheme(theme: Int) {
        return put(THEME, theme)
    }

    fun getNightUpdateHour(): Int {
        return get(NIGHT_UPDATE_HOUR, Int::class.java)
    }

    fun setNightUpdateHour(theme: Int) {
        return put(NIGHT_UPDATE_HOUR, theme)
    }

    fun getNightUpdateMinute(): Int {
        return get(NIGHT_UPDATE_MINUTE, Int::class.java)
    }

    fun setNightUpdateMinute(theme: Int) {
        return put(NIGHT_UPDATE_MINUTE, theme)
    }

    fun getTilesData(): DownloadInfoResponse? {
        return getObject<DownloadInfoResponse>(TILES_DATA)
    }

    fun saveTilesData(tiles: DownloadInfoResponse) {
        return putObject<DownloadInfoResponse>(TILES_DATA, tiles)
    }

    fun getDownloadId(): Long {
        return get(DOWNLOAD_ID, Long::class.java)
    }

    fun setDownloadId(downloadID: Long) {
        return put(DOWNLOAD_ID, downloadID)
    }

    fun saveFacilityCode(email: String) {
        put(REMEMBER_CODE, email)
    }

    fun getFacilityCode(): String {
        return get(REMEMBER_CODE, String::class.java)
    }
    fun saveExitKioskCode(email: String) {
        put(EXIT_KIOSK_CODE, email)
    }

    fun getExitKioskCode(): String {
        return get(EXIT_KIOSK_CODE, String::class.java)
    }

    fun saveToken(token: String) {
        put(PREF_TOKEN, token)
    }

    fun getToken(): String {
        return get(PREF_TOKEN, String::class.java)
    }

    fun getIsLogin(): Boolean {
        return get(IS_LOGIN, Boolean::class.java)
    }

    fun setIsLogin(flag: Boolean) {
        put(IS_LOGIN, flag)
    }

    fun getZipExtracted(): Boolean {
        return get(ZIP_EXTRACTED, Boolean::class.java)
    }

    fun setZipExtracted(flag: Boolean) {
        put(ZIP_EXTRACTED, flag)
    }

    fun getIsKiosk(): Boolean {
        return get(IS_KIOSK, Boolean::class.java)
    }

    fun setIsKiosk(flag: Boolean) {
        put(IS_KIOSK, flag)
    }
    fun getIsFirstLaunch(): Boolean {
        return get(IS_FIRST_LAUNCH, Boolean::class.java)
    }

    fun setIsFirstLaunch(flag: Boolean) {
        put(IS_FIRST_LAUNCH, flag)
    }
    fun getIsWifiOn(): Boolean {
        return get(IS_WIFI, Boolean::class.java)
    }

    fun setIsWifiOn(flag: Boolean) {
        put(IS_WIFI, flag)
    }

    fun getPreviousWifiSSID(): String {
        return get(PREVIOUS_WIFI_SSID, String::class.java)
    }

    fun setPreviousWifiSSID(value: String) {
        put(PREVIOUS_WIFI_SSID, value)
    }

    fun getSimHeader(): String {
        return get(SIM_HEADER, String::class.java)
    }

    fun setSimHeader(value: String) {
        put(SIM_HEADER, value)
    }
    fun getIsGolfAppLaunch(): Boolean {
        return get(IS_GOLF_APP_LAUNCH, Boolean::class.java)
    }

    fun setIsGolfAppLaunch(flag: Boolean) {
        put(IS_GOLF_APP_LAUNCH, flag)
    }

    private fun <T> get(key: String, clazz: Class<T>): T =
        when (clazz) {
            String::class.java -> sharedPref.getString(key, "")
            Boolean::class.java -> sharedPref.getBoolean(key, false)
            Float::class.java -> sharedPref.getFloat(key, -1f)
            Double::class.java -> sharedPref.getFloat(key, -1f)
            Int::class.java -> sharedPref.getInt(key, -1)
            Long::class.java -> sharedPref.getLong(key, -1L)
            else -> null
        } as T

    private fun <T> put(key: String, data: T) {
        val editor = sharedPref.edit()
        when (data) {
            is String -> editor.putString(key, data)
            is Boolean -> editor.putBoolean(key, data)
            is Float -> editor.putFloat(key, data)
            is Double -> editor.putFloat(key, data.toFloat())
            is Int -> editor.putInt(key, data)
            is Long -> editor.putLong(key, data)
        }
        editor.apply()
    }

    /**
     * Saves object into the Preferences.
     *
     * @param `object` Object of model class (of type [T]) to save
     * @param key Key with which Shared preferences to
     **/
    private fun <T> putObject(key: String, `object`: T) {
        val jsonString = GsonBuilder().create().toJson(`object`)
        val editor = sharedPref.edit()
        editor.putString(key, jsonString).apply()
    }

    /**
     * Used to retrieve object from the Preferences.
     *
     * @param key Shared Preference key with which object was saved.
     **/
    inline fun <reified T> getObject(key: String): T? {
        val value = sharedPref.getString(key, null)
        //JSON String was found which means object can be read.
        //We convert this JSON String to model object. Parameter "c" (of
        //type Class < T >" is used to cast.
        return GsonBuilder().create().fromJson(value, T::class.java)
    }

    fun clear() {
        sharedPref.edit().run {
            remove(PREF_TOKEN)
        }.apply()
    }

    fun clearAll() {
        for (key in sharedPref.all.keys) {
            if (!key.contentEquals(THEME)) {
                sharedPref.edit().run {
                    remove(key)
                }.apply()
            }
        }
    }


}