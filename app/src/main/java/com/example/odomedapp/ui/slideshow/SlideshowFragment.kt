package com.example.odomedapp.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.odomedapp.DatabaseHelper
import com.example.odomedapp.R
import com.example.odomedapp.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val dbHelper = DatabaseHelper(requireContext()) // Asegúrate de que el constructor acepte contexto
        val textViewRoles: TextView = binding.textViewRoles

        // Obtener roles y mostrar en TextView
        val roles = dbHelper.getRoles()

        val users = dbHelper.getAllUsers()
        textViewRoles.text = users.joinToString("\n") { "${it.nombres} (${it.email})" }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}