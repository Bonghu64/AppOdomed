package com.example.odomedapp
import com.example.odomedapp.data.User

import android.content.Context
import android.database.Cursor
import android.os.StrictMode
import android.util.Log
import com.example.odomedapp.data.Cita
import java.sql.*
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(requireContext: Context) {

    private val url = "jdbc:mysql://192.168.0.5:3306/clinicaodontologica1"
    private val user = "nuevo_usuario"
    private val password = "tu_contraseña"

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

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        Log.d("DatabaseHelper", "Iniciando conexión a MySQL")
        Class.forName("com.mysql.jdbc.Driver")  // Asegura la carga del driver JDBC
        val connection: Connection = DriverManager.getConnection(url, user, password)
        Log.d("DatabaseHelper", "Conexión establecida exitosamente")

        val statement: Statement = connection.createStatement()
        val resultSet: ResultSet? = statement.executeQuery("SELECT * FROM usuarios")

        while (resultSet?.next() == true) {
            val id = resultSet.getInt("id_usuario")
            val name = resultSet.getString("nombres")
            val email = resultSet.getString("email")
            users.add(User(id, name, email))
        }

        resultSet?.close()
        statement.close()
        connection.close()

        return users
    }
    // File: DatabaseHelper.kt
    fun getAllCitas(): List<Cita> {
        val citas = mutableListOf<Cita>()
        Log.d("DatabaseHelper", "Iniciando conexión a MySQL")
        Class.forName("com.mysql.jdbc.Driver")  // Asegura la carga del driver JDBC

        val connection: Connection = DriverManager.getConnection(url, user, password)
        Log.d("DatabaseHelper", "Conexión establecida exitosamente")

        val statement: Statement = connection.createStatement()
        val resultSet: ResultSet? = statement.executeQuery("SELECT * FROM citas")

        while (resultSet?.next() == true) {
            val idCita = resultSet.getInt("id_cita")
            val fecha = resultSet.getString("fecha")
            val idPaciente = resultSet.getInt("id_paciente").takeIf { !resultSet.wasNull() }
            val idOdontologo = resultSet.getInt("id_odontologo").takeIf { !resultSet.wasNull() }
            val idRecepcionista = resultSet.getInt("id_recepcionista").takeIf { !resultSet.wasNull() }
            val estadoCita = resultSet.getString("estado_cita")
            val idCosto = resultSet.getInt("id_costo").takeIf { !resultSet.wasNull() }
            val idHorario = resultSet.getInt("id_horario").takeIf { !resultSet.wasNull() }
            val activo = resultSet.getInt("activo") == 1

            citas.add(Cita(idCita, fecha, idPaciente, idOdontologo, idRecepcionista, estadoCita, idCosto, idHorario, activo))
        }

        resultSet?.close()
        statement.close()
        connection.close()

        return citas
    }





}