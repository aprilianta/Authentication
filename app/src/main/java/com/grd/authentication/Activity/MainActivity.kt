package com.grd.authentication.Activity

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.grd.authentication.R
import com.grd.authentication.SharedPrefManager
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    lateinit var sharedPrefManager: SharedPrefManager
    lateinit var alertDialogBuilder: MaterialAlertDialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPrefManager = SharedPrefManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miLogout -> {
                alertDialogBuilder = MaterialAlertDialogBuilder(this)
                alertDialogBuilder.setTitle("Are you sure?")
                alertDialogBuilder.setMessage("You'll logged out from this application.")

                alertDialogBuilder.setPositiveButton("Agree") { dialog, which ->
                    sharedPrefManager.saveSP(SharedPrefManager.sp_logged_in, false)
                    startActivity(
                        Intent(this@MainActivity, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    FirebaseAuth.getInstance().signOut();
                    finish()
                }

                alertDialogBuilder.setNegativeButton("Disagree") { dialog, which ->

                }

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
