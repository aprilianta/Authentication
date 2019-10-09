package com.grd.authentication.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.grd.authentication.Model.LoginModel
import com.grd.authentication.R
import com.grd.authentication.Retrofit.Api
import com.grd.authentication.Retrofit.ApiClient
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    lateinit var username: String
    lateinit var password: String
    lateinit var mApiInterface: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mApiInterface = ApiClient.client!!.create(Api::class.java)
        btnLogin.setOnClickListener({ requestlogin() })
    }

    fun requestlogin() {
        username = etUsername.text.toString()
        password = etPassword.text.toString()
        val loginModelCall = mApiInterface.cek(username, password)
        loginModelCall.enqueue(object : Callback<LoginModel> {
            override fun onResponse(call: Call<LoginModel>, response: Response<LoginModel>) {
                val value = response.body().value
                val message = response.body().message
                //progress.dismiss();
                if (value == "1") {
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    val i = Intent(this@LoginActivity, MainActivity::class.java)
//                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error connection!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
