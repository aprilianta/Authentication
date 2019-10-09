package com.grd.authentication

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {

    internal var sp: SharedPreferences
    internal var spEditor: SharedPreferences.Editor

    val spUsername: String
        get() = sp.getString(sp_username, "")!!

    val spLoginStatus: Boolean?
        get() = sp.getBoolean(sp_logged_in, false)

    init {
        sp = context.getSharedPreferences(sp_app, Context.MODE_PRIVATE)
        spEditor = sp.edit()
    }

    fun saveSPString(keySP: String, value: String) {
        spEditor.putString(keySP, value)
        spEditor.commit()
    }

    fun saveSPInt(keySP: String, value: Int) {
        spEditor.putInt(keySP, value)
        spEditor.commit()
    }

    fun saveSP(keySP: String, value: Boolean) {
        spEditor.putBoolean(keySP, value)
        spEditor.commit()
    }

    companion object {
        val sp_app = "spAApp"
        val sp_username = "spusername"
        val sp_logged_in = "spLoggedIn"
    }
}