package com.example.odomedapp.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.databinding.FragmentGalleryBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.odomedapp.MainActivity
import com.example.odomedapp.data.Cita
import com.example.odomedapp.ui.adapters.CitaAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SlideshowFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoTransparente: View
    private lateinit var cuadroProgress: LinearLayout
    private lateinit var btonCrear: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        swipeRefreshLayout = binding.swipeRefreshLayout
        recyclerView = binding.recyclerViewUsers
        progressBar = binding.progressBar
        fondoTransparente = binding.fondoTransparente
        cuadroProgress = binding.cuadroProgress
        btonCrear = binding.btnCrearCita

        btonCrear.visibility = View.GONE

        // Configurar SwipeRefreshLayout para actualizar datos
        swipeRefreshLayout.setOnRefreshListener {
            cargarDatos()
        }

        // Cargar datos cuando el fragmento se muestra
        cargarDatos()

        return root
    }

    private fun cargarDatos() {
        // Muestra la capa de fondo transparente y el cuadro de progreso
        fondoTransparente.visibility = View.VISIBLE
        cuadroProgress.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        (activity as? MainActivity)?.disableMenu()

        // Bloquea la interacción con la interfaz
        recyclerView.isEnabled = false
        swipeRefreshLayout.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            // Simulando una operación larga con base de datos
            val citasList = withContext(Dispatchers.IO) {
                DatabaseHelper(requireContext()).getAllCitas()
            }

            // Filtrar las citas para mostrar solo las de hoy o pasadas
            val citasFiltradas = citasList.filter { cita ->
                val citaFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(cita.fecha)
                val hoy = Calendar.getInstance().time
                citaFecha?.before(hoy) == true || citaFecha?.equals(hoy) == true
            }

            // Crear el adaptador con las citas filtradas
            val newAdapter = CitaAdapter(citasFiltradas, { cita ->
                cancelarCita(cita)
            }, requireContext())

            recyclerView.adapter = newAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Ocultar la capa de fondo y el cuadro de progreso
            fondoTransparente.visibility = View.GONE
            cuadroProgress.visibility = View.GONE
            progressBar.visibility = View.GONE

            // Vuelve a habilitar la interacción
            recyclerView.isEnabled = true
            swipeRefreshLayout.isEnabled = true

            // Detiene la animación del SwipeRefreshLayout
            swipeRefreshLayout.isRefreshing = false
            (activity as? MainActivity)?.enableMenu()

        }
    }

    private fun cancelarCita(cita: Cita) {
        // Lógica de cancelación de citas...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
