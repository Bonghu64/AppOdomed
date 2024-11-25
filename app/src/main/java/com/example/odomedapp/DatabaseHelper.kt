package com.example.odomedapp

import android.content.Context
import android.os.StrictMode
import android.util.Log
import java.sql.*
import com.example.odomedapp.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(requireContext: Context) {

    private val url = "jdbc:mysql://:22966/clinicaodontologica"
    private val userDB = ""
    private val passwordDB = ""

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    suspend fun getAllCitas(): List<Cita> {
        return withContext(Dispatchers.IO) {
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

            citas
        }
    }


    suspend fun loginUser(email: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            var user: User? = null
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
            user
        }
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


    fun getOdontologos(): List<Odontologo> {
        val odontologos = mutableListOf<Odontologo>()
        val query = """
        SELECT o.id_odontologo, o.numero_licencia, o.especializacion, o.activo, 
               u.nombres, u.apellidos
        FROM odontologos o
        INNER JOIN usuarios u ON o.id_odontologo = u.id_usuario
        WHERE o.activo = 1 AND u.activo = 1
    """

        try {
            val connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement(query)
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val odontologo = Odontologo(
                    idOdontologo = resultSet.getInt("id_odontologo"),
                    numeroLicencia = resultSet.getString("numero_licencia"),
                    especializacion = resultSet.getString("especializacion"),
                    activo = resultSet.getBoolean("activo"),
                    nombres = resultSet.getString("nombres"),
                    apellidos = resultSet.getString("apellidos")
                )
                odontologos.add(odontologo)
            }

            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return odontologos
    }


    fun getAvailableCitas(date: String, odontologoId: Int): List<Cita> {
        val citas = mutableListOf<Cita>()
        val connection = DriverManager.getConnection(url, userDB, passwordDB)
        val statement = connection.prepareStatement(
            "SELECT * FROM citas WHERE fecha = ? AND id_odontologo = ? AND id_paciente IS NULL"
        )
        statement.setString(1, date)
        statement.setInt(2, odontologoId)

        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            citas.add(
                Cita(
                    idCita = resultSet.getInt("id_cita"),
                    fecha = resultSet.getString("fecha"),
                    idPaciente = null,
                    idOdontologo = resultSet.getInt("id_odontologo"),
                    estadoCita = resultSet.getString("estado_cita"),
                    idRecepcionista = resultSet.getInt("id_recepcionista"),
                    idCosto = resultSet.getInt("id_costo"),
                    idHorario = resultSet.getInt("id_horario"),
                    activo = resultSet.getBoolean("activo")
                )
            )
        }
        resultSet.close()
        statement.close()
        connection.close()
        return citas
    }
    fun updateCita(idHorario: Int, idOdontologo: Int, idPaciente: Int, fecha: String): Boolean {
        try {
            val connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement(
                "UPDATE citas SET id_paciente = ? WHERE id_horario = ? AND id_odontologo = ? AND fecha = ?"
            )
            statement.setInt(1, idPaciente)
            statement.setInt(2, idHorario)
            statement.setInt(3, idOdontologo)
            statement.setString(4, fecha) // Asegúrate de que "fecha" sea del tipo correcto en tu base de datos

            val rowsUpdated = statement.executeUpdate()
            Log.d("UpdateCita", "Filas actualizadas: $rowsUpdated")

            statement.close()
            connection.close()

            return rowsUpdated > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    fun getHorarioById(idHorario: Int?): Horario? {
        if (idHorario == null) return null

        var horario: Horario? = null
        val connection = DriverManager.getConnection(url, userDB, passwordDB)
        val statement = connection.prepareStatement(
            "SELECT * FROM horarios WHERE id_horario = ?"
        )
        statement.setInt(1, idHorario)

        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            horario = Horario(
                idHorario = resultSet.getInt("id_horario"),
                horario = resultSet.getString("horario")
            )
        }

        resultSet.close()
        statement.close()
        connection.close()

        return horario
    }

    fun cancelarCita(idCita: Int): Boolean {
        var isUpdated = false
        try {
            Class.forName("com.mysql.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement(
                "UPDATE citas SET id_paciente = NULL WHERE id_cita = ? AND activo = 1"
            )
            statement.setInt(1, idCita)

            isUpdated = statement.executeUpdate() > 0

            statement.close()
            connection.close()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al cancelar la cita", e)
        }
        return isUpdated
    }
    // Ejemplo de función para obtener el odontólogo por su ID
    fun obtenerOdontologoPorId(idOdontologo: Int): User? {
        var user: User? = null
        try {
            val connection = DriverManager.getConnection(url, userDB, passwordDB)
            val statement = connection.prepareStatement(
                "SELECT u.nombres, u.apellidos FROM usuarios u " +
                        "INNER JOIN odontologos o ON u.id_usuario = o.id_odontologo " +
                        "WHERE o.id_odontologo = ?"
            )
            statement.setInt(1, idOdontologo)

            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                val nombres = resultSet.getString("nombres")
                val apellidos = resultSet.getString("apellidos")
                user = User(idUsuario = idOdontologo, nombres = nombres, apellidos = apellidos, email = "", rolId = 0, activo = true)
            }

            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            e.printStackTrace() // Para imprimir el error en el log
        }
        return user
    }


}