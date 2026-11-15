package com.AzoguePablo.gestorpersonaldetareas// Usa tu paquete real

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.AzoguePablo.gestorpersonaldetareas.model.PreferenciasUsuario
import com.AzoguePablo.gestorpersonaldetareas.model.TareaDatabase
import com.AzoguePablo.gestorpersonaldetareas.ui.PantallaTareas
import com.AzoguePablo.gestorpersonaldetareas.ui.theme.GestorPersonalDeTareasTheme
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModel
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Obtenemos la instancia única (Singleton) de la base de datos
        val database = TareaDatabase.getDatabase(applicationContext)

        // 2. Instanciamos el ViewModel inyectándole el DAO mediante la Factory
        val preferencias = PreferenciasUsuario(applicationContext)
        val viewModel: TareaViewModel by viewModels {
            TareaViewModelFactory(database.tareaDao(), preferencias)
        }

        // 3. Pintamos la interfaz declarativa de Jetpack Compose
        setContent {
            GestorPersonalDeTareasTheme {
                PantallaTareas(viewModel = viewModel)
            }
        }
    }
}