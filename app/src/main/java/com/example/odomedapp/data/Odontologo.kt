package com.example.odomedapp.data

data class Odontologo(
    val idOdontologo: Int,
    val numeroLicencia: String,
    val especializacion: String?,
    val activo: Boolean,
    val nombres: String,
    val apellidos: String
)

