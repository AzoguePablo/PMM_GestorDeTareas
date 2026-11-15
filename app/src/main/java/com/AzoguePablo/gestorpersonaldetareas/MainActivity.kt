package com.AzoguePablo.gestorpersonaldetareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.AzoguePablo.gestorpersonaldetareas.data.TareaDatabase
import com.AzoguePablo.gestorpersonaldetareas.preferences.PreferenciasUsuario
import com.AzoguePablo.gestorpersonaldetareas.ui.PantallaApi
import com.AzoguePablo.gestorpersonaldetareas.ui.PantallaTareas
import com.AzoguePablo.gestorpersonaldetareas.ui.theme.GestorPersonalDeTareasTheme
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModel
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModelFactory
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareasApiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicialización de Room y DataStore
        val database = TareaDatabase.getDatabase(applicationContext)
        val preferencias = PreferenciasUsuario(applicationContext)

        // 2. Instanciación de ViewModels (persistencia local y consumo de API)
        val viewModelLocal: TareaViewModel by viewModels {
            TareaViewModelFactory(database.tareaDao(), preferencias)
        }
        val viewModelApi: TareasApiViewModel by viewModels()

        setContent {
            GestorPersonalDeTareasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Estado persistente ante cambios de configuración (giro de pantalla)
                    var pantallaActual by rememberSaveable { mutableStateOf("LOCAL") }

                    if (pantallaActual == "LOCAL") {
                        PantallaTareas(
                            viewModel = viewModelLocal,
                            onIrAApi = { pantallaActual = "API" }
                        )
                    } else {
                        BackHandler { pantallaActual = "LOCAL" }
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