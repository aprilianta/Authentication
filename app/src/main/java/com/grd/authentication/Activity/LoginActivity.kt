package com.grd.authentication.Activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.grd.authentication.Model.LoginModel
import com.grd.authentication.R
import com.grd.authentication.Retrofit.Api
import com.grd.authentication.Retrofit.ApiClient
import com.grd.authentication.SharedPrefManager
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    lateinit var username: String
    lateinit var password: String
    lateinit var mApiInterface: Api
    lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPrefManager = SharedPrefManager(this)
        mApiInterface = ApiClient.client!!.create(Api::class.java)
        btnLogin.setOnClickListener({ requestlogin() })

        if (sharedPrefManager.spLoginStatus!!) {
            val i = Intent(this@LoginActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
        }
    }

    fun requestlogin() {
        username = etUsername.text.toString()
        password = etPassword.text.toString()

        if (username.isEmpty()) {
            etUsername.error = "Username required"
            etUsername.requestFocus()
        }

        if (password.isEmpty()) {
            etPassword.error = "Password required"
            etPassword.requestFocus()
        }

        if (!username.isEmpty() && !password.isEmpty()) {
            progressBar.visibility = View.VISIBLE
            val loginModelCall = mApiInterface.cek(username, password)
            loginModelCall.enqueue(object : Callback<LoginModel> {
                override fun onResponse(call: Call<LoginModel>, response: Response<LoginModel>) {
                    val value = response.body().value
                    val message = response.body().message
                    if (value == "1") {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                        val i = Intent(this@LoginActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                        sharedPrefManager.saveSP(SharedPrefManager.sp_logged_in, true)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error connection!", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}
