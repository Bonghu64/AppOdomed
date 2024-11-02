package com.example.odomedapp.data

data class User(
    val idUsuario: Int,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val rolId: Int,
    val activo: Boolean
)
