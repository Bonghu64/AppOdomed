package com.example.odomedapp.ui.gallery

import HorarioAdapter
import OdontologoAdapter
import android.app.DatePickerDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

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
        binding.cardHora.visibility = View.GONE
        binding.cardOdon.visibility = View.GONE

        // Configurar selección de fecha
        binding.fechaTextView.setOnClickListener {
            showDatePicker()
        }

        // Configurar botón de guardar
        binding.guardarButton.setOnClickListener {
            guardarCita()
        }

        // Configurar botón de volver
        binding.volverButton.setOnClickListener {
            onBackPressed()  // Este método se llama cuando el botón de volver es presionado
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Establecer la fecha mínima como un día después de la fecha actual
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val minDate = calendar.timeInMillis

        // Guardamos la fecha mínima
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.DAY_OF_MONTH, 1) // Día siguiente
        val minYear = minCalendar.get(Calendar.YEAR)
        val minMonth = minCalendar.get(Calendar.MONTH)
        val minDay = minCalendar.get(Calendar.DAY_OF_MONTH)

        // Establecer la fecha máxima como dos semanas después de la fecha actual
        calendar.add(Calendar.DAY_OF_MONTH, 14)  // Sumar 14 días
        val maxDate = calendar.timeInMillis

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = "$year-${month + 1}-$dayOfMonth"
                binding.fechaTextView.text = selectedDate
                loadOdontologos() // Cargar odontólogos para la fecha seleccionada
            },
            minYear,  // Usamos el año de la fecha mínima
            minMonth, // Usamos el mes de la fecha mínima
            minDay    // Usamos el día de la fecha mínima
        )

        // Establecer la fecha mínima y máxima en el DatePicker
        val datePickerDialog = datePicker.datePicker
        datePickerDialog.minDate = minDate
        datePickerDialog.maxDate = maxDate

        // Mostrar el DatePicker
        datePicker.show()
    }





    private fun loadOdontologos() {
        // Mostrar el ProgressBar
        binding.progressBar.visibility = View.VISIBLE

        // Deshabilitar la interacción con los Spinners, fecha, y botón guardar
        binding.odontologoSpinner.isEnabled = false
        binding.fechaTextView.isEnabled = false
        binding.guardarButton.isEnabled = false
        binding.volverButton.isEnabled = false// Deshabilitar el botón guardar

        // Ejecutar la operación de base de datos en una corutina
        lifecycleScope.launch {
            try {
                // Ejecutar la consulta en un hilo de fondo
                val odontologosList = withContext(Dispatchers.IO) {
                    dbHelper.getOdontologos() // Obtén odontólogos desde la base de datos
                }

                if (odontologosList.isNotEmpty()) {
                    // Configurar el adaptador personalizado para odontólogos
                    val adapter = OdontologoAdapter(this@CrearCitaActivity, R.layout.odontologo_spinner_item, odontologosList)
                    binding.odontologoSpinner.adapter = adapter

                    // Mostrar el spinner de odontólogos
                    binding.odontologoSpinner.visibility = View.VISIBLE
                    binding.cardOdon.visibility = View.VISIBLE
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
                    Toast.makeText(this@CrearCitaActivity, "No hay odontólogos disponibles", Toast.LENGTH_SHORT).show()
                    binding.odontologoSpinner.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrearCitaActivity, "Error al cargar odontólogos", Toast.LENGTH_SHORT).show()
            } finally {
                // Ocultar el ProgressBar y habilitar la interacción nuevamente
                binding.progressBar.visibility = View.GONE
                binding.odontologoSpinner.isEnabled = true
                binding.fechaTextView.isEnabled = true
                binding.guardarButton.isEnabled = true // Habilitar el botón guardar
                binding.volverButton.isEnabled = true
            }
        }
    }

    private fun loadHorarios(date: String, odontologoId: Int) {
        // Mostrar el ProgressBar
        binding.progressBar.visibility = View.VISIBLE

        // Deshabilitar interacción con el spinner de odontólogos, fecha y botón guardar
        binding.odontologoSpinner.isEnabled = false
        binding.fechaTextView.isEnabled = false
        binding.guardarButton.isEnabled = false
        binding.volverButton.isEnabled = false

        // Ejecutar la operación de base de datos en una corutina
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

                Log.d("Horarios", "Horarios cargados: ${horariosList.map { it.idHorario }}")

                if (horariosList.isNotEmpty()) {
                    val adapter = HorarioAdapter(this@CrearCitaActivity, horariosList)
                    binding.horarioSpinner.adapter = adapter

                    binding.horarioSpinner.visibility = View.VISIBLE
                    binding.cardHora.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@CrearCitaActivity, "No hay horarios disponibles", Toast.LENGTH_SHORT).show()
                    binding.horarioSpinner.visibility = View.GONE
                    binding.cardHora.visibility = View.GONE
                }

            } catch (e: Exception) {
                Log.e("LoadHorariosError", "Error al cargar los horarios", e)
                Toast.makeText(this@CrearCitaActivity, "Error al cargar horarios", Toast.LENGTH_SHORT).show()
            } finally {
                // Ocultar el ProgressBar y habilitar la interacción nuevamente
                binding.progressBar.visibility = View.GONE
                binding.odontologoSpinner.isEnabled = true
                binding.fechaTextView.isEnabled = true
                binding.guardarButton.isEnabled = true
                binding.volverButton.isEnabled = true

            }
        }
    }







    private fun guardarCita() {
        if (selectedDate == null) {
            Toast.makeText(this, "Debe elegir una fecha válida", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedOdontologo == null) {
            Toast.makeText(this, "Debe elegir un odontólogo válido", Toast.LENGTH_SHORT).show()
            return
        }

        val horarioSeleccionado = binding.horarioSpinner.selectedItem as? Horario
        if (horarioSeleccionado == null) {
            Toast.makeText(this, "Debe elegir un horario válido", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.horarioSpinner.visibility != View.VISIBLE || binding.horarioSpinner.adapter == null) {
            Toast.makeText(this, "Debe elegir un horario válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Bloquear UI temporalmente mientras se guarda la cita
        binding.odontologoSpinner.isEnabled = false
        binding.fechaTextView.isEnabled = false
        binding.guardarButton.isEnabled = false
        binding.horarioSpinner.isEnabled = false
        binding.volverButton.isEnabled = false

        // Obtener el ID del usuario logueado (paciente)
        val userId = SessionManager.getUser()?.idUsuario ?: return

        // Actualizar la cita en la base de datos
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = dbHelper.updateCita(
                    horarioSeleccionado.idHorario,
                    selectedOdontologo!!.idOdontologo,
                    userId,
                    selectedDate!!
                )
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
                Log.e("GuardarCitaError", "Error al guardar cita", e)
            } finally {
                // Restaurar interacción con la UI
                withContext(Dispatchers.Main) {
                    binding.odontologoSpinner.isEnabled = true
                    binding.fechaTextView.isEnabled = true
                    binding.guardarButton.isEnabled = true
                    binding.horarioSpinner.isEnabled = true
                    binding.volverButton.isEnabled = true
                }
            }
        }
    }



}
