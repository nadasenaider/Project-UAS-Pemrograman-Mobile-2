package com.example.cinelog.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "cinelog_pref"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_COMPLETED_ONBOARDING = "completed_onboarding"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_IMAGE = "user_image"
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean(KEY_COMPLETED_ONBOARDING, completed).apply()
    }

    fun isCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_COMPLETED_ONBOARDING, false)
    }

    fun saveUserSession(name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Rina Ramadhani") ?: "Rina Ramadhani"
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "rina@cinelog.com") ?: "rina@cinelog.com"
    }

    fun saveUserImage(imageUri: String) {
        prefs.edit().putString(KEY_USER_IMAGE, imageUri).apply()
    }

    fun getUserImage(): String? {
        return prefs.getString(KEY_USER_IMAGE, null)
    }

    fun clearSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putString(KEY_USER_NAME, null)
            putString(KEY_USER_EMAIL, null)
            apply()
        }
    }
}
