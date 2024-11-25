// File: CitaAdapter.kt
package com.example.odomedapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.data.Cita
import com.example.odomedapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CitaAdapter(
    val citasList: List<Cita>,
    private val onCancelarClick: (Cita) -> Unit,
    private val context: Context // Pasamos el contexto para la consulta de la base de datos
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val horario: TextView = view.findViewById(R.id.horarioTextView)
        val fecha: TextView = view.findViewById(R.id.fechaTextView)
        val estado: TextView = view.findViewById(R.id.estadoTextView)
        val odontologo: TextView = view.findViewById(R.id.odontologoTextView) // Nuevo TextView para odontólogo
        val btnCancelar: Button = view.findViewById(R.id.btnCancelar) // El botón de cancelar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citasList[position]

        // Convertir la fecha en formato "dd-Mes-aaaa"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = dateFormat.parse(cita.fecha)

        val fechaFormateada = SimpleDateFormat("dd-MMMM-yyyy", Locale("es", "ES")).format(fecha)

        // Asignar la fecha formateada al TextView
        holder.fecha.text = fechaFormateada

        holder.estado.text = cita.estadoCita

        // Obtener el nombre del odontólogo de la base de datos usando un coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val odontologo = DatabaseHelper(context).obtenerOdontologoPorId(cita.idOdontologo ?: 0)
            val horario = DatabaseHelper(context).getHorarioById(cita.idHorario)

            // Actualizar la UI en el hilo principal
            withContext(Dispatchers.Main) {
                if (odontologo != null) {
                    holder.odontologo.text = "Dr. ${odontologo.nombres} ${odontologo.apellidos}"
                } else {
                    holder.odontologo.text = "Odontólogo no encontrado"
                }

                // Mostrar el horario si existe
                // Formateamos la hora en formato 12 horas con AM/PM
                val horaFormateada = horario?.horario?.let {
                    val horaFormat = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
                    val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it) // Asumimos que la hora está en formato 24 horas
                    hora?.let { horaFormat.format(hora) } ?: "Hora no disponible"
                } ?: "Horario no disponible"
                holder.horario.text = "Hora: $horaFormateada"
            }
        }

        // Verificar si la cita es para hoy o después
        val citaFecha = dateFormat.parse(cita.fecha)
        val hoy = Calendar.getInstance().time
        val isCancelable = citaFecha?.after(hoy) ?: false || citaFecha?.equals(hoy) == true

        // Mostrar o esconder el botón de cancelar
        holder.btnCancelar.visibility = if (isCancelable) View.VISIBLE else View.GONE

        // Manejamos el click en el botón de cancelar
        holder.btnCancelar.setOnClickListener {
            onCancelarClick(cita)
            // Deshabilitar el botón de cancelar inmediatamente después de hacer clic
            holder.btnCancelar.isEnabled = false
        }
    }




    override fun getItemCount() = citasList.size
}