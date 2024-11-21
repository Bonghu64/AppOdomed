package com.example.odomedapp.ui.gallery

import HorarioAdapter
import OdontologoAdapter
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.R
import com.example.odomedapp.data.*
import com.example.odomedapp.databinding.ActivityCrearCitaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            // Configurar el adaptador personalizado para odontólogos
            val adapter = OdontologoAdapter(this, R.layout.odontologo_spinner_item, odontologosList)
            binding.odontologoSpinner.adapter = adapter

            // Mostrar el spinner de odontólogos
            binding.odontologoSpinner.visibility = View.VISIBLE
            binding.odontologoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Asignar el odontólogo seleccionado según su posición en la lista
                    selectedOdontologo = odontologosList[position]
                    loadHorarios(selectedDate!!, selectedOdontologo!!.idOdontologo)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedOdontologo = null
                }
            }
        } else {
            Toast.makeText(this, "No hay odontólogos disponibles", Toast.LENGTH_SHORT).show()
            binding.odontologoSpinner.visibility = View.GONE
        }
    }



    private fun loadHorarios(date: String, odontologoId: Int) {
        // Ocultar el spinner de horarios hasta que se carguen los datos
        binding.horarioSpinner.visibility = View.GONE

        // Ejecutar la operación de base de datos en una corrutina
        lifecycleScope.launch {
            try {
                // Cambiar al contexto de IO para operaciones de base de datos
                val horariosList = withContext(Dispatchers.IO) {
                    val citasList = dbHelper.getAvailableCitas(date, odontologoId)
                    citasList.mapNotNull { cita ->
                        dbHelper.getHorarioById(cita.idHorario)?.let { horario ->
                            Horario(
                                idHorario = horario.idHorario,
                                horario = horario.horario // Ajustar si "horario" tiene otro nombre
                            )
                        }
                    }
                }

                if (horariosList.isNotEmpty()) {
                    // Configurar el adaptador personalizado para horarios
                    val adapter = HorarioAdapter(this@CrearCitaActivity, horariosList)
                    binding.horarioSpinner.adapter = adapter

                    // Mostrar el spinner de horarios
                    binding.horarioSpinner.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@CrearCitaActivity, "No hay horarios disponibles", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrearCitaActivity, "Error al cargar horarios", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun guardarCita() {
        // Verificar si hay un odontólogo seleccionado
        if (selectedOdontologo == null) {
            Toast.makeText(this, "Debe elegir un odontólogo válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si hay un horario seleccionado
        val horarioSeleccionado = binding.horarioSpinner.selectedItem as? Horario
        if (horarioSeleccionado == null) {
            Toast.makeText(this, "Debe elegir un horario válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el ID del usuario logueado (paciente)
        val userId = SessionManager.getUser()?.idUsuario ?: return

        // Actualizar la cita en la base de datos
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = dbHelper.updateCita(horarioSeleccionado.idHorario, userId)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@CrearCitaActivity, "Cita programada exitosamente", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CrearCitaActivity, "Error al programar la cita", Toast.LENGTH_SHORT).show()

                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CrearCitaActivity, "Error al programar la cita", Toast.LENGTH_SHORT).show()
                }
                Log.e("asas","a", e)
            }
        }
    }


}
