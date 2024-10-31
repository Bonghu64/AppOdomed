package com.example.odomedapp.data

data class Cita(
    val idCita: Int,
    val fecha: String,
    val idPaciente: Int?,
    val idOdontologo: Int?,
    val idRecepcionista: Int?,
    val estadoCita: String,
    val idCosto: Int?,
    val idHorario: Int?,
    val activo: Boolean
)
