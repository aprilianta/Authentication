package com.grd.authentication.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.grd.authentication.Model.LoginModel
import com.grd.authentication.R
import com.grd.authentication.Retrofit.Api
import com.grd.authentication.Retrofit.ApiClient
import com.grd.authentication.SharedPrefManager
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import com.facebook.FacebookSdk;

class LoginActivity : AppCompatActivity() {

    lateinit var username: String
    lateinit var password: String
    lateinit var mApiInterface: Api
    lateinit var sharedPrefManager: SharedPrefManager
    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private var callbackManager: CallbackManager? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPrefManager = SharedPrefManager(this)
        mApiInterface = ApiClient.client!!.create(Api::class.java)
        btnLogin.setOnClickListener({ requestlogin() })
        firebaseAuth = FirebaseAuth.getInstance()
        configureGoogleSignIn()
        google_button.setOnClickListener({ googlesignin() })
        createKeyHash(this, "com.grd.authentication")
        fb_button.setOnClickListener({ fbsignin() })
    }

    private fun createKeyHash(activity: Activity, yourPackage: String) {
        val info = activity.packageManager.getPackageInfo(yourPackage, PackageManager.GET_SIGNATURES)
        for (signature in info.signatures) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
        }
    }

    private fun fbsignin() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d("coeg", "Facebook token: " + loginResult.accessToken.token)
                    val i = Intent(this@LoginActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    Toast.makeText(this@LoginActivity, "Success", Toast.LENGTH_SHORT).show()
                    startActivity(i)
                    progressBar.visibility = View.INVISIBLE
                    finish()
                }

                override fun onCancel() {
                    Toast.makeText(this@LoginActivity, "Facebook sign in failed", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@LoginActivity, "Facebook sign in error", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
            })
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private fun googlesignin() {
        progressBar.visibility = View.VISIBLE
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
//                    progressBar.visibility = View.VISIBLE
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show()
            }
        }
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val i = Intent(this@LoginActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                Toast.makeText(this@LoginActivity, "Success", Toast.LENGTH_SHORT).show()
                startActivity(i)
                progressBar.visibility = View.INVISIBLE
                finish()
            } else {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = firebaseAuth.currentUser
        if (user != null || sharedPrefManager.spLoginStatus!!) {
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
