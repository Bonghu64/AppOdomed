package com.example.odomedapp.ui.gallery

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.odomedapp.databinding.ActivityCrearCitaBinding

class CrearCitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearCitaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilitar el botón de regreso en la barra de acciones
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Aquí va la lógica para el formulario de creación de citas
    }

    // Configurar el botón de regreso para finalizar el activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Finalizar el activity y regresar al fragmento de citas
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}