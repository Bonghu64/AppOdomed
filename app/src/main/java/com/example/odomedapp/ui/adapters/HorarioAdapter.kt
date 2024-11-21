import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import android.widget.ArrayAdapter
import com.example.odomedapp.R
import com.example.odomedapp.data.Horario
import java.text.SimpleDateFormat
import java.util.Locale

class HorarioAdapter(context: Context, private val horarios: List<Horario>) :
    ArrayAdapter<Horario>(context, 0, horarios) {

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
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(time)
        return outputFormat.format(date ?: "")
    }
}
