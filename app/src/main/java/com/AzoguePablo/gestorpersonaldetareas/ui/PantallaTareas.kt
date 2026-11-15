package com.AzoguePablo.gestorpersonaldetareas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AzoguePablo.gestorpersonaldetareas.model.Prioridad
import com.AzoguePablo.gestorpersonaldetareas.model.Tarea
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModel
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaTareas(viewModel: TareaViewModel) {
    val listaTareas by viewModel.tareas.collectAsStateWithLifecycle()
    val soloPendientes by viewModel.preferencias.soloPendientesFlow.collectAsStateWithLifecycle(initialValue = false)
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Tareas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir tarea")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 2. CAMBIO AQUÍ: Borramos los chips y ponemos el Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrar solo pendientes",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = soloPendientes,
                    onCheckedChange = { nuevoEstado -> viewModel.actualizarFiltro(nuevoEstado) }
                )
            }

            if (listaTareas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 3. CAMBIO AQUÍ: Simplificamos el texto al nuevo sistema true/false
                    Text(
                        text = if (soloPendientes) {
                            "No tienes tareas pendientes."
                        } else {
                            "No hay tareas registradas. ¡Añade una nueva!"
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = listaTareas,
                        key = { tarea -> tarea.id }
                    ) { tarea ->
                        ItemTarea(
                            tarea = tarea,
                            onToggleCompletada = { viewModel.alternarCompletada(tarea) },
                            onBorrar = { viewModel.borrarTarea(tarea) }
                        )
                    }
                }
            }
        }

        if (mostrarDialogo) {
            DialogoNuevaTarea(
                onDismiss = { mostrarDialogo = false },
                onConfirmar = { titulo, descripcion, prioridad ->
                    // Fíjate que el ViewModel anterior usaba "agregarTarea", ahora en la nueva versión es "insertarTarea"
                    // Modificamos esto para construir la Tarea antes de pasarla
                    viewModel.insertarTarea(Tarea(titulo = titulo, descripcion = descripcion, prioridad = prioridad))
                    mostrarDialogo = false
                }
            )
        }
    }
}
fun compartirTarea(context: Context, tarea: Tarea) {
    // 1. Preparamos el texto tal y como pide el enunciado
    val textoACompartir = """
        Tarea: ${tarea.titulo}
        Descripción: ${tarea.descripcion.ifBlank { "Sin descripción" }}
        Prioridad: ${tarea.prioridad.name}
    """.trimIndent()

    // 2. Creamos el Intent implícito de tipo ACTION_SEND
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textoACompartir)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir tarea mediante...")
    context.startActivity(shareIntent)
}
@Composable
fun ItemTarea(
    tarea: Tarea,
    onToggleCompletada: () -> Unit,
    onBorrar: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onToggleCompletada() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = tarea.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null
                )
                if (tarea.descripcion.isNotBlank()) {
                    Text(
                        text = tarea.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Prioridad: ${tarea.prioridad.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (tarea.prioridad) {
                        Prioridad.ALTA -> MaterialTheme.colorScheme.error
                        Prioridad.MEDIA -> MaterialTheme.colorScheme.primary
                        Prioridad.BAJA -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
            IconButton(onClick = { compartirTarea(context, tarea) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir tarea"
                )
            }

            IconButton(onClick = onBorrar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar tarea",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DialogoNuevaTarea(
    onDismiss: () -> Unit,
    onConfirmar: (String, String, Prioridad) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var prioridadSeleccionada by remember { mutableStateOf(Prioridad.MEDIA) }
    var mostrarError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        mostrarError = false
                    },
                    label = { Text("Título *") },
                    isError = mostrarError,
                    singleLine = true
                )
                if (mostrarError) {
                    Text(
                        text = "El título es obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") }
                )

                Text("Prioridad:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Prioridad.entries.forEach { prioridad ->
                        FilterChip(
                            selected = prioridadSeleccionada == prioridad,
                            onClick = { prioridadSeleccionada = prioridad },
                            label = { Text(prioridad.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        mostrarError = true
                    } else {
                        onConfirmar(titulo, descripcion, prioridadSeleccionada)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}