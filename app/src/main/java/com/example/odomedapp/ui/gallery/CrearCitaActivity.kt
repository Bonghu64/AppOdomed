package com.example.odomedapp.ui.gallery

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.odomedapp.data.SessionManager
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.data.Horario
import com.example.odomedapp.data.Odontologo
import com.example.odomedapp.data.Paciente
import com.example.odomedapp.databinding.ActivityCrearCitaBinding
import java.util.Calendar

class CrearCitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearCitaBinding
    private val dbHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilitar el botón de regreso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = SessionManager.getUser()
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val roleId = user.rolId

        // Mostrar u ocultar campos basados en el rol del usuario
        configureFieldsByRole(roleId)

        // Configurar selección de fecha
        binding.fechaTextView.setOnClickListener {
            showDatePicker()
        }

        // Cargar opciones de horarios
        loadHorarios()

        // Configurar el botón de guardar
        binding.guardarButton.setOnClickListener {
            guardarCita(roleId)
        }
    }

    private fun configureFieldsByRole(roleId: Int) {
        when (roleId) {
            1, 2 -> { // Odontólogo o Recepcionista
                binding.pacienteSpinner.visibility = View.VISIBLE
                binding.estadoSpinner.visibility = View.VISIBLE
                if (roleId == 2) {
                    binding.odontologoSpinner.visibility = View.VISIBLE
                    loadOdontologos()
                }
                loadPacientes()
                loadEstados()
            }
            3 -> { // Paciente
                binding.estadoSpinner.visibility = View.GONE
                binding.pacienteSpinner.visibility = View.GONE
                binding.odontologoSpinner.visibility = View.GONE
            }
            4 -> { // Practicante
                Toast.makeText(this, "No tienes permiso para registrar citas", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadHorarios() {
        val horariosList = dbHelper.getHorarios()
        val horariosAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horariosList)
        horariosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.horarioSpinner.adapter = horariosAdapter
    }

    private fun loadPacientes() {
        val pacientesList = dbHelper.getPacientes()
        val pacientesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pacientesList)
        pacientesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.pacienteSpinner.adapter = pacientesAdapter
    }

    private fun loadOdontologos() {
        val odontologosList = dbHelper.getOdontologos()
        val odontologosAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, odontologosList)
        odontologosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.odontologoSpinner.adapter = odontologosAdapter
    }

    private fun loadEstados() {
        val estados = listOf("programada", "completada", "cancelada")
        val estadosAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        estadosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.estadoSpinner.adapter = estadosAdapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                binding.fechaTextView.text = "$year-${month + 1}-$dayOfMonth"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun guardarCita(roleId: Int) {
        val fecha = binding.fechaTextView.text.toString()
        if (fecha.isEmpty()) {
            Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show()
            return
        }

        val idHorario = (binding.horarioSpinner.selectedItem as Horario).idHorario
        var idPaciente: Int? = null
        var idOdontologo: Int? = null
        var idRecepcionista: Int? = null
        var estadoCita = "programada" // Valor por defecto

        when (roleId) {
            1 -> { // Odontólogo
                idPaciente = (binding.pacienteSpinner.selectedItem as Paciente).idPaciente
                idOdontologo = dbHelper.getOdontologoIdByUserId(SessionManager.getUser()!!.idUsuario)
                estadoCita = binding.estadoSpinner.selectedItem.toString()
            }
            2 -> { // Recepcionista
                idPaciente = (binding.pacienteSpinner.selectedItem as Paciente).idPaciente
                idOdontologo = (binding.odontologoSpinner.selectedItem as Odontologo).idOdontologo
                idRecepcionista = dbHelper.getRecepcionistaIdByUserId(SessionManager.getUser()!!.idUsuario)
                estadoCita = binding.estadoSpinner.selectedItem.toString()
            }
            3 -> { // Paciente
                idPaciente = dbHelper.getPacienteIdByUserId(SessionManager.getUser()!!.idUsuario)
            }
        }

        // Guardar la cita en la base de datos
        val result = dbHelper.insertCita(
            fecha, idPaciente, idOdontologo, idRecepcionista, estadoCita, null, idHorario
        )

        if (result) {
            Toast.makeText(this, "Cita guardada exitosamente", Toast.LENGTH_SHORT).show()
            finish() // Regresar al fragmento de citas
        } else {
            Toast.makeText(this, "Error al guardar la cita", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
