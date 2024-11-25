package com.example.odomedapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.odomedapp.data.SessionManager
import com.example.odomedapp.databinding.ActivityMainBinding
import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.TextView
import androidx.navigation.ui.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SessionManager
        SessionManager.initialize(this)

        // Establecer el toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        // Configurar el DrawerLayout y NavigationView
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Configurar la barra de navegación
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Obtener el usuario guardado en SessionManager
        val user = SessionManager.getUser()

        // Si el usuario está logueado, actualizar el nav header
        if (user != null) {
            val navHeader = navView.getHeaderView(0)
            val textViewName = navHeader.findViewById<TextView>(R.id.textViewName)
            val textViewEmail = navHeader.findViewById<TextView>(R.id.textViewEmail)

            // Establecer el nombre y correo del usuario
            textViewName.text = "${user.nombres} ${user.apellidos}"
            textViewEmail.text = user.email
        }

// Configurar las acciones para las demás opciones del menú
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logoutUser() // Manejar manualmente el logout
                    true
                }
                else -> {
                    // Para otros destinos, permitir que NavigationUI lo maneje
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        binding.drawerLayout.closeDrawers()
                    }
                    handled
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logoutUser() {
        val sessionManager = SessionManager // Asegúrate de usar tu clase SessionManager correctamente
        val user = sessionManager.getUser()

        // Limpia la sesión
        sessionManager.clearUser()

        // Muestra un mensaje de despedida
        val userName = user?.nombres ?: "Usuario" // Ajusta esto según tu modelo de datos
        Toast.makeText(this, "Hasta luego, $userName", Toast.LENGTH_LONG).show()

        // Redirige al login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    // En MainActivity
    fun disableMenu() {
        val menu = binding.navView.menu
        for (i in 0 until menu.size()) {
            menu.getItem(i).isEnabled = false
        }
    }

    fun enableMenu() {
        val menu = binding.navView.menu
        for (i in 0 until menu.size()) {
            menu.getItem(i).isEnabled = true
        }
    }


}