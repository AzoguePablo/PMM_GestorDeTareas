package com.AzoguePablo.gestorpersonaldetareas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AzoguePablo.gestorpersonaldetareas.network.ClienteRetrofit
import com.AzoguePablo.gestorpersonaldetareas.network.TareaApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Definimos los estados exactos que puede tener nuestra pantalla según el enunciado
sealed class EstadoApi {
    object Inactivo : EstadoApi() // Estado inicial (botón esperando ser pulsado)
    object Cargando : EstadoApi() // Petición en curso (mostrar indicador de carga)
    data class Exito(val tareas: List<TareaApi>) : EstadoApi() // Petición completada con datos
    data class Error(val mensaje: String) : EstadoApi() // Fallo de conexión
}

class TareasApiViewModel : ViewModel() {

    // 2. Flujo de estado interno y público
    private val _estado = MutableStateFlow<EstadoApi>(EstadoApi.Inactivo)
    val estado: StateFlow<EstadoApi> = _estado.asStateFlow()

    // 3. Función para descargar las tareas
    fun obtenerTareasDeLaRed() {
        // Evitamos peticiones simultáneas si ya está cargando
        if (_estado.value is EstadoApi.Cargando) return

        _estado.value = EstadoApi.Cargando

        // Usamos una corrutina para no bloquear el hilo principal
        viewModelScope.launch {
            try {
                // Hacemos la petición a la red
                val tareas = ClienteRetrofit.api.obtenerTareas()
                // Si va bien, pasamos al estado de Éxito con la lista de tareas
                _estado.value = EstadoApi.Exito(tareas)
            } catch (e: Exception) {
                // Si falla (por ejemplo, no hay WiFi), pasamos al estado de Error
                _estado.value = EstadoApi.Error("No se pudo conectar al servidor. Comprueba tu conexión a Internet.")
            }
        }
    }
}