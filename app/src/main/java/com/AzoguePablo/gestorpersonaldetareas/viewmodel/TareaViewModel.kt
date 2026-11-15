package com.AzoguePablo.gestorpersonaldetareas.viewmodel // Asegúrate de que tu paquete sea correcto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.AzoguePablo.gestorpersonaldetareas.model.PreferenciasUsuario
import com.AzoguePablo.gestorpersonaldetareas.model.Tarea
import com.AzoguePablo.gestorpersonaldetareas.model.TareaDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TareaViewModel(
    private val dao: TareaDao,
    val preferencias: PreferenciasUsuario // Inyectamos DataStore
) : ViewModel() {

    // Combinamos el flujo de Room con el flujo de DataStore
    val tareas: StateFlow<List<Tarea>> = combine(
        dao.obtenerTodasLasTareas(),
        preferencias.soloPendientesFlow
    ) { listaTareas, soloPendientes ->
        if (soloPendientes) {
            listaTareas.filter { !it.completada } // Filtramos si el Switch está activado
        } else {
            listaTareas // Devolvemos todas si el Switch está desactivado
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Función para que la interfaz guarde la preferencia cuando el usuario toque el Switch
    fun actualizarFiltro(soloPendientes: Boolean) {
        viewModelScope.launch {
            preferencias.guardarFiltroPendientes(soloPendientes)
        }
    }

    // --- El resto de funciones CRUD de Room se quedan exactamente igual ---
    fun insertarTarea(tarea: Tarea) {
        viewModelScope.launch { dao.insertarTarea(tarea) }
    }

    fun borrarTarea(tarea: Tarea) {
        viewModelScope.launch { dao.borrarTarea(tarea) }
    }
    fun actualizarTarea(tarea: Tarea) {
        viewModelScope.launch { dao.actualizarTarea(tarea) }
    }

    fun alternarCompletada(tarea: Tarea) {
        viewModelScope.launch {
            dao.insertarTarea(tarea.copy(completada = !tarea.completada))
        }
    }
}

// Actualizamos la Factory para que reciba también las Preferencias
class TareaViewModelFactory(
    private val dao: TareaDao,
    private val preferencias: PreferenciasUsuario
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TareaViewModel(dao, preferencias) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}