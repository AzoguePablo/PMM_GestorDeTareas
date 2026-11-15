package com.AzoguePablo.gestorpersonaldetareas.preferences
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Creamos la instancia única del DataStore usando una extensión sobre Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracion_tareas")

class PreferenciasUsuario(private val context: Context) {

    // 2. Definimos la clave (como si fuera el nombre de la columna en una base de datos)
    private val MOSTRAR_SOLO_PENDIENTES = booleanPreferencesKey("mostrar_solo_pendientes")

    // 3. Flujo para leer el valor en tiempo real
    val soloPendientesFlow: Flow<Boolean> = context.dataStore.data
        .map { preferencias ->
            // Si es la primera vez que se abre y no hay nada guardado, devuelve false (mostrar todas)
            preferencias[MOSTRAR_SOLO_PENDIENTES] ?: false
        }

    // 4. Función suspendida para guardar el nuevo valor cuando el usuario toque el Switch
    suspend fun guardarFiltroPendientes(soloPendientes: Boolean) {
        context.dataStore.edit { preferencias ->
            preferencias[MOSTRAR_SOLO_PENDIENTES] = soloPendientes
        }
    }
}