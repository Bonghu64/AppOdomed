package com.example.odomedapp.ui.gallery

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.data.*
import com.example.odomedapp.databinding.ActivityCrearCitaBinding
import java.util.Calendar

class CrearCitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearCitaBinding
    private val dbHelper = DatabaseHelper(this)
    private var selectedDate: String? = null
    private var selectedOdontologo: Odontologo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = SessionManager.getUser()
        if (user == null || user.rolId != 3) { // Solo pacientes (rol 3)
            Toast.makeText(this, "No tienes permiso para crear citas", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // Inicialmente ocultar odontólogo y horario
        binding.odontologoSpinner.visibility = View.GONE
        binding.horarioSpinner.visibility = View.GONE

        // Configurar selección de fecha
        binding.fechaTextView.setOnClickListener {
            showDatePicker()
        }

        // Configurar botón de guardar
        binding.guardarButton.setOnClickListener {
            guardarCita()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = "$year-${month + 1}-$dayOfMonth"
                binding.fechaTextView.text = selectedDate
                loadOdontologos() // Cargar odontólogos para la fecha seleccionada
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun loadOdontologos() {
        val odontologosList = dbHelper.getOdontologos() // Obtén odontólogos desde la base de datos
        if (odontologosList.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, odontologosList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.odontologoSpinner.adapter = adapter

            // Mostrar odontólogo y configurar selección
            binding.odontologoSpinner.visibility = View.VISIBLE
            binding.odontologoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedOdontologo = odontologosList[position]
                    loadHorarios(selectedDate!!, selectedOdontologo!!.idOdontologo)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Lógica si no se selecciona nada (opcional)
                }
            }
        } else {
            Toast.makeText(this, "No hay odontólogos disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadHorarios(date: String, odontologoId: Int) {
        val horariosList = dbHelper.getAvailableCitas(date, odontologoId) // Citas disponibles
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horariosList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.horarioSpinner.adapter = adapter

        // Mostrar horarios disponibles
        binding.horarioSpinner.visibility = View.VISIBLE
    }

    private fun guardarCita() {
        val horario = binding.horarioSpinner.selectedItem as? Cita ?: return
        val userId = SessionManager.getUser()?.idUsuario ?: return

        val success = dbHelper.updateCita(horario.idCita, userId)
        if (success) {
            Toast.makeText(this, "Cita programada exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al programar la cita", Toast.LENGTH_SHORT).show()
        }
    }
}
