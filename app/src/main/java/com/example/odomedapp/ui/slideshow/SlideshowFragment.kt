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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.odomedapp.databinding.FragmentHomeBinding
import com.example.odomedapp.ui.adapters.UserAdapter

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configuración de RecyclerView
        recyclerView = binding.recyclerViewUsers
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Obtener usuarios y configurar el adaptador
        val dbHelper = DatabaseHelper(requireContext())
        val users = dbHelper.getAllUsers()
        userAdapter = UserAdapter(users)
        recyclerView.adapter = userAdapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}