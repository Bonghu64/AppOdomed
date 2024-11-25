package com.example.odomedapp.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.MainActivity
import com.example.odomedapp.data.Cita
import com.example.odomedapp.databinding.DialogConfirmCancelBinding
import com.example.odomedapp.databinding.FragmentGalleryBinding
import com.example.odomedapp.ui.adapters.CitaAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoTransparente: View
    private lateinit var cuadroProgress: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        swipeRefreshLayout = binding.swipeRefreshLayout
        recyclerView = binding.recyclerViewUsers
        progressBar = binding.progressBar
        fondoTransparente = binding.fondoTransparente
        cuadroProgress = binding.cuadroProgress


        // Configurar SwipeRefreshLayout para actualizar datos
        swipeRefreshLayout.setOnRefreshListener {
            cargarDatos()
        }

        // Cargar datos cuando el fragmento se muestra
        cargarDatos()
        binding.btnCrearCita.setOnClickListener {
            val intent = Intent(activity, CrearCitaActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    private fun cargarDatos() {
        // Deshabilitar el menú
        (activity as? MainActivity)?.disableMenu()

        // Muestra la capa de fondo transparente y el cuadro de progreso
        fondoTransparente.visibility = View.VISIBLE
        cuadroProgress.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        // Bloquea la interacción con la interfaz
        recyclerView.isEnabled = false
        swipeRefreshLayout.isEnabled = false
        binding.btnCrearCita.isEnabled = false

        // Si ya tienes un adapter, puedes deshabilitar los botones de cada item
        val adapter = recyclerView.adapter as? CitaAdapter
        adapter?.let {
            it.citasList.forEach { cita ->
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(it.citasList.indexOf(cita)) as? CitaAdapter.CitaViewHolder
                viewHolder?.btnCancelar?.isEnabled = false
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            // Simulando una operación larga con base de datos
            val citasList = withContext(Dispatchers.IO) {
                DatabaseHelper(requireContext()).getAllCitas()
            }

            // Filtrar las citas para mostrar solo las de mañana en adelante
            val today = LocalDate.now()
            val citasFiltradas = citasList.filter { cita ->
                val citaFecha = LocalDate.parse(cita.fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd")) // Ajusta el formato según tu fecha
                citaFecha.isAfter(today)
            }

            // Actualiza la interfaz con los datos filtrados
            val newAdapter = CitaAdapter(citasFiltradas, { cita ->
                cancelarCita(cita)
            }, requireContext())  // Aquí pasamos el contexto

            recyclerView.adapter = newAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Ocultar la capa de fondo y el cuadro de progreso
            fondoTransparente.visibility = View.GONE
            cuadroProgress.visibility = View.GONE
            progressBar.visibility = View.GONE

            // Habilitar la interacción
            recyclerView.isEnabled = true
            swipeRefreshLayout.isEnabled = true
            binding.btnCrearCita.isEnabled = true

            // Detiene la animación del SwipeRefreshLayout
            swipeRefreshLayout.isRefreshing = false

            // Habilitar el menú
            (activity as? MainActivity)?.enableMenu()
        }
    }



    private fun cancelarCita(cita: Cita) {
        val dialog = ConfirmCancelDialogFragment()

        dialog.onConfirmListener = {
            // Deshabilitar los botones en el diálogo de confirmación inmediatamente al confirmar
            dialog.binding?.btnConfirmar?.isEnabled = false
            dialog.binding?.btnCancelar?.isEnabled = false
            dialog.binding?.btnCancelar?.isEnabled = false

            // Proceder con la cancelación de la cita
            cancelAppointment(cita)
        }

        dialog.show(requireActivity().supportFragmentManager, "ConfirmCancelDialog")
    }

    private fun cancelAppointment(cita: Cita) {
        // Mostrar el ProgressBar y bloquear la interacción
        fondoTransparente.visibility = View.VISIBLE
        cuadroProgress.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        recyclerView.isEnabled = false
        swipeRefreshLayout.isEnabled = false
        binding.btnCrearCita.isEnabled = false

        // También deshabilitamos los botones en el diálogo de confirmación
        val dialog = fragmentManager?.findFragmentByTag("ConfirmCancelDialog") as? ConfirmCancelDialogFragment
        dialog?.binding?.btnConfirmar?.isEnabled = false
        dialog?.binding?.btnCancelar?.isEnabled = false

        // Deshabilitar todos los botones "Cancelar" en el RecyclerView
        val adapter = recyclerView.adapter as? CitaAdapter
        val viewHolders = (0 until recyclerView.childCount).map { recyclerView.getChildAt(it) }
        for (viewHolderView in viewHolders) {
            val viewHolder = recyclerView.getChildViewHolder(viewHolderView) as? CitaAdapter.CitaViewHolder
            viewHolder?.btnCancelar?.isEnabled = false // Deshabilitar el botón "Cancelar" en cada ViewHolder
        }

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                // Simula la cancelación de la cita en la base de datos
                DatabaseHelper(requireContext()).cancelarCita(cita.idCita)
            }

            // Ocultar el ProgressBar y desbloquear la UI
            fondoTransparente.visibility = View.GONE
            cuadroProgress.visibility = View.GONE
            progressBar.visibility = View.GONE

            // Habilitar la interacción con la UI nuevamente
            recyclerView.isEnabled = true
            swipeRefreshLayout.isEnabled = true
            binding.btnCrearCita.isEnabled = true

            // Volver a habilitar los botones en el diálogo
            dialog?.binding?.btnConfirmar?.isEnabled = true
            dialog?.binding?.btnCancelar?.isEnabled = true

            if (result) {
                // Recargar los datos de las citas después de cancelar
                cargarDatos()

                // Mostrar un mensaje de éxito
                Toast.makeText(requireContext(), "Cita cancelada", Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar un mensaje de error si la cancelación falló
                Toast.makeText(requireContext(), "Error al cancelar la cita", Toast.LENGTH_SHORT).show()
            }
        }
    }






    override fun onResume() {
        super.onResume()
        // Recargar los datos cada vez que se entra al fragmento
        cargarDatos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
class ConfirmCancelDialogFragment : DialogFragment() {
    var onConfirmListener: (() -> Unit)? = null
    var binding: DialogConfirmCancelBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogConfirmCancelBinding.inflate(inflater, container, false)

        binding?.btnCancelar?.setOnClickListener {
            dismiss()  // Cierra el diálogo si se presiona Cancelar
        }

        binding?.btnConfirmar?.setOnClickListener {
            onConfirmListener?.invoke()  // Llama al listener de confirmación
            dismiss()
        }

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}



