package com.vizio.viziobleoobe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Navigate to MainActivity after a delay or immediately
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Close SplashActivity
    }
}