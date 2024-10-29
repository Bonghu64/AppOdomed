package com.example.odomedapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Conectar elementos de la interfaz
        val textViewWelcome: TextView = binding.textViewWelcome
        val buttonStart: Button = binding.buttonStart

        // Agregar funcionalidad al botón
        buttonStart.setOnClickListener {
            textViewWelcome.text = "¡Has iniciado la aplicación!"
        }

        val textViewRoles: TextView = binding.textViewRoles
        val dbHelper = DatabaseHelper(requireContext()) // Asegúrate de que el constructor acepte contexto

        // Obtener roles y mostrar en TextView
        val roles = dbHelper.getRoles()
        textViewRoles.text = roles.joinToString("\n") { it }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
