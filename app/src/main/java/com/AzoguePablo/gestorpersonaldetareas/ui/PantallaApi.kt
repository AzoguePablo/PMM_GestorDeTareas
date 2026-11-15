package com.AzoguePablo.gestorpersonaldetareas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AzoguePablo.gestorpersonaldetareas.network.TareaApi
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.EstadoApi
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareasApiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaApi(
    viewModel: TareasApiViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas de ejemplo") },
                navigationIcon = {
                    Button(onClick = onVolver, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón de carga deshabilitado mientras se realiza la petición (Requisito 3.6)
            Button(
                onClick = { viewModel.obtenerTareasDeLaRed() },
                enabled = estado !is EstadoApi.Cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (estado is EstadoApi.Cargando) "Cargando..."
                    else "Descargar tareas del servidor"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (val estadoActual = estado) {
                    is EstadoApi.Inactivo -> {
                        Text("Pulsa el botón superior para consultar las tareas de ejemplo.")
                    }
                    is EstadoApi.Cargando -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Conectando con el servidor...")
                        }
                    }
                    is EstadoApi.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = estadoActual.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(onClick = { viewModel.obtenerTareasDeLaRed() }) {
                                Text("Reintentar conexión")
                            }
                        }
                    }
                    is EstadoApi.Exito -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(estadoActual.tareas,key = { it.id }) { tareaApi ->
                                ItemTareaApi(tareaApi)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemTareaApi(tarea: TareaApi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "ID Servidor: ${tarea.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tarea.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (tarea.completada) "Estado: Finalizada" else "Estado: Pendiente",
                style = MaterialTheme.typography.bodyMedium,
                color = if (tarea.completada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}