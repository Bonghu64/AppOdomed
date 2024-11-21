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
import java.text.SimpleDateFormat
import java.util.Locale

class HorarioAdapter(context: Context, @LayoutRes private val resource: Int, private val horarios: List<String>) :
    ArrayAdapter<String>(context, resource, horarios) {

    private val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(resource, parent, false)

        val horarioTextView = view.findViewById<TextView>(android.R.id.text1)
        val formattedTime = formatter.format(SimpleDateFormat("HH:mm", Locale.getDefault()).parse(horarios[position])!!)
        horarioTextView.text = formattedTime

        return view
    }
}
