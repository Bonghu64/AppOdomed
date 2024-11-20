package com.example.odomedapp
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la ActionBar

        setContentView(R.layout.activity_splash_screen)

        // Retrasar 5 segundos antes de pasar al LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Navegar a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finaliza la SplashScreenActivity para no volver atrás
        }, 5000) // 5000 ms = 5 segundos
    }
}