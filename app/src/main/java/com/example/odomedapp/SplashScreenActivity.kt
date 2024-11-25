package com.example.odomedapp
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.odomedapp.data.SessionManager

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.initialize(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        supportActionBar?.hide()

        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (SessionManager.isLoggedIn()) {
                // Si el usuario está logueado, redirige a MainActivity
                Intent(this, MainActivity::class.java)
            } else {
                // Si no está logueado, redirige a LoginActivity
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 5000)
    }
}
