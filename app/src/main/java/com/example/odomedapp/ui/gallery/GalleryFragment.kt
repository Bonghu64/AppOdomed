package com.example.odomedapp.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.databinding.FragmentGalleryBinding
import com.example.odomedapp.ui.adapters.CitaAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        // Mostrar el ProgressBar y el fondo gris mientras se obtienen los datos
        fondoTransparente.visibility = View.VISIBLE
        cuadroProgress.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        // Obtener los datos en segundo plano
        CoroutineScope(Dispatchers.Main).launch {
            val citasList = withContext(Dispatchers.IO) {
                DatabaseHelper(requireContext()).getAllCitas()
            }

            // Configurar el RecyclerView con las citas obtenidas
            val adapter = CitaAdapter(citasList)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Ocultar el ProgressBar, el cuadro y el fondo cuando termine la operación
            fondoTransparente.visibility = View.GONE
            cuadroProgress.visibility = View.GONE
            progressBar.visibility = View.GONE

            // Detener la animación de refresco
            swipeRefreshLayout.isRefreshing = false
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

