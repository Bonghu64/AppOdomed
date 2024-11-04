package com.example.odomedapp

import android.content.Context
import android.database.Cursor
import android.os.StrictMode
import android.util.Log
import java.sql.*
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.odomedapp.data.*

class DatabaseHelper(requireContext: Context) {

    private val url = "jdbc:mysql://192.168.0.6:3306/clinicaodontologica1"
    private val userDB = "nuevo_usuario"
    private val passwordDB = "tu_contraseña"

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    fun getRoles(): List<String> {
        val rolesList = mutableListOf<String>()
        try {
            Log.d("DatabaseHelper", "Iniciando conexión a MySQL")
            Class.forName("com.mysql.jdbc.Driver")  // Asegura la carga del driver JDBC
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
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


    // File: DatabaseHelper.kt
    fun getAllCitas(): List<Cita> {
        val citas = mutableListOf<Cita>()
        Log.d("DatabaseHelper", "Iniciando conexión a MySQL")
        var user: User? = null
        Class.forName("com.mysql.jdbc.Driver") // Carga el driver JDBC
        val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)

        val idUsuarioCitas = SessionManager.getUser()?.idUsuario
        val idRolCitas = SessionManager.getUser()?.rolId
        var statement: PreparedStatement? = null
        when(idRolCitas){
            1 -> {
                statement = connection.prepareStatement("SELECT * FROM citas WHERE id_odontologo = ? AND activo = 1")
                if (idUsuarioCitas != null) {
                    statement.setInt(1, idUsuarioCitas)
                }

            }
            3 -> {
                statement = connection.prepareStatement("SELECT * FROM citas WHERE id_paciente = ? AND activo = 1")
                if (idUsuarioCitas != null) {
                    statement.setInt(1, idUsuarioCitas)
                }
            }
        }


        val resultSet: ResultSet? = statement?.executeQuery() ?: null

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
        if (statement != null) {
            statement.close()
        }
        connection.close()

        return citas
    }

    fun loginUser(email: String, password: String): User? {
        var user: User? = null
        Class.forName("com.mysql.jdbc.Driver") // Carga el driver JDBC
        val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
        val statement = connection.prepareStatement("SELECT * FROM usuarios WHERE email = ? AND contrasenia = ? AND activo = 1")

        statement.setString(1, email)
        statement.setString(2, password)

        val resultSet: ResultSet = statement.executeQuery()

        if (resultSet.next()) {
            val id = resultSet.getInt("id_usuario")
            val nombres = resultSet.getString("nombres")
            val apellidos = resultSet.getString("apellidos")
            val rolId = resultSet.getInt("rol_id")
            val activo = resultSet.getBoolean("activo")

            user = User(id, nombres, apellidos, email, rolId, activo)

            // Guardamos el usuario en SessionManager para acceso en toda la app
            SessionManager.saveUser(user)
        }

        resultSet.close()
        statement.close()
        connection.close()

        return user
    }

    // File: DatabaseHelper.kt

    fun insertCita(
        fecha: String,
        idPaciente: Int?,
        idOdontologo: Int?,
        idRecepcionista: Int?,
        estadoCita: String,
        idCosto: Int?,
        idHorario: Int?
    ): Boolean {
        var isInserted = false
        try {
            Class.forName("com.mysql.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement(
                "INSERT INTO citas (fecha, id_paciente, id_odontologo, id_recepcionista, estado_cita, id_costo, id_horario, activo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            )

            statement.setString(1, fecha)
            statement.setObject(2, idPaciente)
            statement.setObject(3, idOdontologo)
            statement.setObject(4, idRecepcionista)
            statement.setString(5, estadoCita)
            statement.setObject(6, idCosto)
            statement.setObject(7, idHorario)
            statement.setInt(8, 1)  // Activo

            isInserted = statement.executeUpdate() > 0

            statement.close()
            connection.close()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al insertar la cita", e)
        }
        return isInserted
    }

    fun getHorarios(): List<Horario> {
        val horariosList = mutableListOf<Horario>()
        try {
            Class.forName("com.mysql.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT id_horario, horario FROM horarios WHERE activo = 1")

            while (resultSet.next()) {
                val idHorario = resultSet.getInt("id_horario")
                val descripcion = resultSet.getString("horario")
                horariosList.add(Horario(idHorario, descripcion))
            }

            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener horarios", e)
        }
        return horariosList
    }

    fun getPacientes(): List<Paciente> {
        val pacientesList = mutableListOf<Paciente>()
        try {
            Class.forName("com.mysql.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT id_paciente, seguro_medico, alergias FROM pacientes WHERE activo = 1")

            while (resultSet.next()) {
                val idPaciente = resultSet.getInt("id_paciente")
                val nombre = resultSet.getString("seguro_medico") + " " + resultSet.getString("alergias")
                pacientesList.add(Paciente(idPaciente, nombre))
            }

            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener pacientes", e)
        }
        return pacientesList
    }

    fun getOdontologos(): List<Odontologo> {
        val odontologosList = mutableListOf<Odontologo>()
        try {
            Class.forName("com.mysql.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT id_odontologo, nombres, apellidos FROM odontologos WHERE activo = 1")

            while (resultSet.next()) {
                val idOdontologo = resultSet.getInt("id_odontologo")
                val nombre = resultSet.getString("nombres") + " " + resultSet.getString("apellidos")
                odontologosList.add(Odontologo(idOdontologo, nombre))
            }

            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener odontólogos", e)
        }
        return odontologosList
    }
    fun getOdontologoIdByUserId(userId: Int): Int? {
        var odontologoId: Int? = null
        try {
            Class.forName("com.mysql.jdbc.Driver") // Asegura la carga del driver JDBC
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement("SELECT * FROM usuarios WHERE id_usuario = ?")

            SessionManager.getUser()?.let { statement.setInt(1, it.idUsuario) }

            val resultSet: ResultSet = statement.executeQuery()
            if (resultSet.next()) {
                odontologoId = resultSet.getInt("id_usuario")
            }

            resultSet.close()
            statement.close()
            connection.close()

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener idOdontologo por userId", e)
        }
        return odontologoId
    }
    fun getRecepcionistaIdByUserId(userId: Int): Int? {
        var recepcionistaId: Int? = null
        try {
            Class.forName("com.mysql.jdbc.Driver") // Asegura la carga del driver JDBC
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement("SELECT id_recepcionista FROM recepcionistas WHERE usuario_id = ?")

            statement.setInt(1, userId)

            val resultSet: ResultSet = statement.executeQuery()
            if (resultSet.next()) {
                recepcionistaId = resultSet.getInt("id_recepcionista")
            }

            resultSet.close()
            statement.close()
            connection.close()

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener idRecepcionista por userId", e)
        }
        return recepcionistaId
    }
    fun getPacienteIdByUserId(userId: Int): Int? {
        var pacienteId: Int? = null
        try {
            Class.forName("com.mysql.jdbc.Driver") // Asegura la carga del driver JDBC
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement("SELECT * FROM usuarios WHERE id_usuario = ?")

            statement.setInt(1, userId)

            val resultSet: ResultSet = statement.executeQuery()
            if (resultSet.next()) {
                pacienteId = resultSet.getInt("id_usuario")
            }

            resultSet.close()
            statement.close()
            connection.close()

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener idPaciente por userId", e)
        }
        return pacienteId
    }


}