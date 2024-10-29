package com.example.odomedapp

import android.content.Context
import android.os.StrictMode
import android.util.Log
import java.sql.*

class DatabaseHelper(requireContext: Context) {

    private val url = "jdbc:mysql://192.168.0.5:3306/clinicaodontologica"
    private val user = "nuevo_usuario"     // Reemplaza con tu usuario de MySQL
    private val password = "tu_contraseña"  // Reemplaza con tu contraseña de MySQL

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    fun getRoles(): List<String> {
        val rolesList = mutableListOf<String>()
        try {
            Log.d("DatabaseHelper", "Iniciando conexión a MySQL")
            Class.forName("com.mysql.jdbc.Driver")  // Asegura la carga del driver JDBC
            val connection: Connection = DriverManager.getConnection(url, user, password)
            Log.d("DatabaseHelper", "Conexión establecida exitosamente")

            val statement: Statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT nombre_rol FROM roles WHERE activo = 1")

            while (resultSet.next()) {
                val role = resultSet.getString("nombre_rol")
                rolesList.add(role)
            }

            resultSet.close()
            statement.close()
            connection.close()
            Log.d("DatabaseHelper", "Conexión cerrada")

        } catch (e: ClassNotFoundException) {
            Log.e("DatabaseHelper", "Error: No se encontró el controlador JDBC", e)
        } catch (e: SQLException) {
            Log.e("DatabaseHelper", "Error de SQL: Fallo en la conexión", e)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Otro error en la conexión", e)
        }
        return rolesList
    }



}