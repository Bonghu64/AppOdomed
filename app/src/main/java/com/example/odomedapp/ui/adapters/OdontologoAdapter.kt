import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import android.widget.ArrayAdapter
import com.example.odomedapp.data.Odontologo
import com.example.odomedapp.R

class OdontologoAdapter(context: Context, @LayoutRes private val resource: Int, private val odontologos: List<Odontologo>) :
    ArrayAdapter<Odontologo>(context, resource, odontologos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(resource, parent, false)

        val odontologo = odontologos[position]
        val nameTextView = view.findViewById<TextView>(R.id.odontologo_name)
        val specializationTextView = view.findViewById<TextView>(R.id.odontologo_specialization)

        nameTextView.text = "Dr. ${odontologo.nombres} ${odontologo.apellidos}"
        specializationTextView.text = odontologo.especializacion ?: "General"

        return view
    }
}
