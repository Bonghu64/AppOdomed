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
        holder.fecha.text = cita.fecha
        holder.estado.text = cita.estadoCita

        // Obtener el nombre del odontólogo de la base de datos usando un coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val odontologo = DatabaseHelper(context).obtenerOdontologoPorId(cita.idOdontologo ?: 0)

            // Actualizar la UI en el hilo principal
            withContext(Dispatchers.Main) {
                if (odontologo != null) {
                    holder.odontologo.text = "Dr. ${odontologo.nombres} ${odontologo.apellidos}"
                } else {
                    holder.odontologo.text = "Odontólogo no encontrado"
                }
            }
        }

        // Verificar si la cita es para hoy o después
        val citaFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cita.fecha)
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