package com.example.odomedapp

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toUpperCase
import java.util.*

import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var progressBar: ProgressBar
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_login)

        databaseHelper = DatabaseHelper(this)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().toUpperCase(Locale.ROOT).trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                startLoginProcess(email, password)
            } else {
                Toast.makeText(this, "Por favor, ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLoginProcess(email: String, password: String) {
        // Mostrar animación y deshabilitar el botón
        progressBar.visibility = View.VISIBLE
        loginButton.isEnabled = false

        // Realizar la operación en segundo plano usando Coroutines
        CoroutineScope(Dispatchers.IO).launch {
            val user = databaseHelper.loginUser(email, password)

            // Actualizar la UI en el hilo principal
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                loginButton.isEnabled = true

                if (user != null) {
                    // Login exitoso
                    Toast.makeText(this@LoginActivity, "Login exitoso: ${user.nombres}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Login fallido
                    Toast.makeText(this@LoginActivity, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

