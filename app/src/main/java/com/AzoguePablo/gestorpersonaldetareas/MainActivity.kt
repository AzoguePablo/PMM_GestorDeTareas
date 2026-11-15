package com.AzoguePablo.gestorpersonaldetareas// Usa tu paquete real

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.AzoguePablo.gestorpersonaldetareas.model.PreferenciasUsuario
import com.AzoguePablo.gestorpersonaldetareas.model.TareaDatabase
import com.AzoguePablo.gestorpersonaldetareas.ui.PantallaApi
import com.AzoguePablo.gestorpersonaldetareas.ui.PantallaTareas
import com.AzoguePablo.gestorpersonaldetareas.ui.theme.GestorPersonalDeTareasTheme
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModel
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModelFactory
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareasApiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Preparamos Room y DataStore
        val database = TareaDatabase.getDatabase(applicationContext)
        val preferencias = PreferenciasUsuario(applicationContext)

        // 2. Preparamos los DOS ViewModels (el de Room y el de Internet)
        val viewModelLocal: TareaViewModel by viewModels {
            TareaViewModelFactory(database.tareaDao(), preferencias)
        }
        val viewModelApi: TareasApiViewModel by viewModels()

        setContent {
            // Usa el nombre de Theme que tengas en tu proyecto (quizás sea distinto a este)
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. LA VARIABLE MAGICA DE NAVEGACIÓN
                    var pantallaActual by remember { mutableStateOf("LOCAL") }

                    // 4. EL CAMBIO DE PANTALLA
                    if (pantallaActual == "LOCAL") {
                        PantallaTareas(
                            viewModel = viewModelLocal,
                            onIrAApi = { pantallaActual = "API" }
                        )
                    } else {
                        PantallaApi(
                            viewModel = viewModelApi,
                            onVolver = { pantallaActual = "LOCAL" }
                        )
                    }
                }
            }
        }
    }
}