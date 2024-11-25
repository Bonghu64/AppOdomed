import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.odomedapp.R
import com.example.odomedapp.data.Horario
import java.text.SimpleDateFormat
import java.util.*

class HorarioAdapter(context: Context, private var horarios: List<Horario>) :
    ArrayAdapter<Horario>(context, 0, horarios) {

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val currentCalendar = Calendar.getInstance()

    fun filterByDateAndTime(date: String) {
        // Obtener fecha y hora actual
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCalendar.time)
        currentCalendar.add(Calendar.HOUR_OF_DAY, -1)
        val adjustedTime = currentCalendar.time

        horarios = if (date == currentDate) {
            horarios.filter { horario ->
                try {
                    val horarioTime = timeFormat.parse(horario.horario)
                    horarioTime?.after(adjustedTime) == true
                } catch (e: Exception) {
                    false // Descarta horarios inválidos
                }
            }
        } else {
            horarios // Mantén todos los horarios si no es la fecha actual
        }

        notifyDataSetChanged() // Notifica cambios al adaptador
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.horario_spinner_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.text_horario)

        val horario = getItem(position)
        horario?.let {
            val formattedTime = formatTimeTo12Hour(it.horario)
            textView.text = formattedTime
        }

        return view
    }

    private fun formatTimeTo12Hour(time: String): String {
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(time)
        return outputFormat.format(date ?: "")
    }
}
