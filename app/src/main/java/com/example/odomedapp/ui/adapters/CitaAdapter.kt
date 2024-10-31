// File: CitaAdapter.kt
package com.example.odomedapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.odomedapp.data.Cita
import com.example.odomedapp.R

class CitaAdapter(private val citasList: List<Cita>) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fecha: TextView = view.findViewById(R.id.textViewFecha)
        val estado: TextView = view.findViewById(R.id.textViewEstado)
        // Añadir otros campos según se requiera
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citasList[position]
        holder.fecha.text = cita.fecha
        holder.estado.text = cita.estadoCita
        // Configurar otros campos aquí
    }

    override fun getItemCount() = citasList.size
}
