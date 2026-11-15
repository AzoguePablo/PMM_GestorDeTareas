package com.AzoguePablo.gestorpersonaldetareas.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AzoguePablo.gestorpersonaldetareas.model.Prioridad
import com.AzoguePablo.gestorpersonaldetareas.model.Tarea
import com.AzoguePablo.gestorpersonaldetareas.viewmodel.TareaViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PantallaTareas(viewModel: TareaViewModel, onIrAApi: () -> Unit) {
    val listaTareas by viewModel.tareas.collectAsStateWithLifecycle()
    val soloPendientes by viewModel.preferencias.soloPendientesFlow.collectAsStateWithLifecycle(initialValue = false)

    // Estados para controlar los diálogos de Crear, Editar y Eliminar
    var mostrarDialogoCrear by rememberSaveable { mutableStateOf(false) }
    var tareaAEditar by remember { mutableStateOf<Tarea?>(null) }
    var tareaAEliminar by remember { mutableStateOf<Tarea?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    Button(onClick = onIrAApi) {
                        Text("Tareas de ejemplo")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoCrear = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir tarea")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Switch persistente con DataStore
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
                            onEditar = { tareaAEditar = tarea },
                            onSolicitarBorrado = { tareaAEliminar = tarea }
                        )
                    }
                }
            }
        }

        // 1. Diálogo para CREAR nueva tarea
        if (mostrarDialogoCrear) {
            DialogoTarea(
                tareaExistente = null,
                onDismiss = { mostrarDialogoCrear = false },
                onConfirmar = { titulo, descripcion, prioridad ->
                    viewModel.insertarTarea(
                        Tarea(
                            titulo = titulo,
                            descripcion = descripcion,
                            prioridad = prioridad
                        )
                    )
                    mostrarDialogoCrear = false
                }
            )
        }

        // 2. Diálogo para EDITAR tarea existente (Requisito 3.3)
        tareaAEditar?.let { tarea ->
            DialogoTarea(
                tareaExistente = tarea,
                onDismiss = { tareaAEditar = null },
                onConfirmar = { titulo, descripcion, prioridad ->
                    viewModel.actualizarTarea(
                        tarea.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            prioridad = prioridad
                        )
                    )
                    tareaAEditar = null
                }
            )
        }

        // 3. AlertDialog de CONFIRMACIÓN DE BORRADO (Requisito 3.3)
        tareaAEliminar?.let { tarea ->
            AlertDialog(
                onDismissRequest = { tareaAEliminar = null },
                title = { Text("Eliminar tarea") },
                text = { Text("¿Seguro que deseas eliminar la tarea \"${tarea.titulo}\"? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.borrarTarea(tarea)
                            tareaAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tareaAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

fun compartirTarea(context: Context, tarea: Tarea) {
    val textoACompartir = """
        Tarea: ${tarea.titulo}
        Descripción: ${tarea.descripcion.ifBlank { "Sin descripción" }}
        Prioridad: ${tarea.prioridad.name}
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textoACompartir)
        type = "text/plain"
    }

    try {
        val shareIntent = Intent.createChooser(sendIntent, "Compartir tarea mediante...")
        context.startActivity(shareIntent)
    } catch (e: ActivityNotFoundException) {
        // Controlamos el caso en el que no exista ninguna app compatible (Requisito 3.7)
        Toast.makeText(
            context,
            "No hay aplicaciones compatibles instaladas para compartir.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun ItemTarea(
    tarea: Tarea,
    onToggleCompletada: () -> Unit,
    onEditar: () -> Unit,
    onSolicitarBorrado: () -> Unit
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

            // Botón Editar
            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar tarea"
                )
            }

            // Botón Compartir
            IconButton(onClick = { compartirTarea(context, tarea) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir tarea"
                )
            }

            // Botón Eliminar (ahora abre el AlertDialog de confirmación)
            IconButton(onClick = onSolicitarBorrado) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar tarea",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoTarea(
    tareaExistente: Tarea?,
    onDismiss: () -> Unit,
    onConfirmar: (String, String, Prioridad) -> Unit
) {
    // Cambiamos remember por rememberSaveable para sobrevivir al giro de pantalla (Prueba 12)
    var titulo by rememberSaveable { mutableStateOf(tareaExistente?.titulo ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(tareaExistente?.descripcion ?: "") }
    var prioridadSeleccionada by rememberSaveable { mutableStateOf(tareaExistente?.prioridad ?: Prioridad.MEDIA) }

    var errorTitulo by rememberSaveable { mutableStateOf<String?>(null) }
    var menuExpandido by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tareaExistente == null) "Nueva Tarea" else "Editar Tarea") },
        text = {
            // Añadimos verticalScroll para que en horizontal o pantallas compactas nunca se corte
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        errorTitulo = null
                    },
                    label = { Text("Título *") },
                    isError = errorTitulo != null,
                    supportingText = {
                        if (errorTitulo != null) {
                            Text(text = errorTitulo!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Menú desplegable obligatorio para la prioridad (Requisito 3.2)
                ExposedDropdownMenuBox(
                    expanded = menuExpandido,
                    onExpandedChange = { menuExpandido = it }
                ) {
                    OutlinedTextField(
                        value = prioridadSeleccionada.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prioridad") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        Prioridad.entries.forEach { prioridad ->
                            DropdownMenuItem(
                                text = { Text(prioridad.name) },
                                onClick = {
                                    prioridadSeleccionada = prioridad
                                    menuExpandido = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !guardando,
                onClick = {
                    val tituloLimpio = titulo.trim()
                    if (tituloLimpio.isEmpty()) {
                        errorTitulo = "El título no puede estar vacío"
                    } else if (tituloLimpio.length < 3) {
                        errorTitulo = "El título debe tener al menos 3 caracteres"
                    } else {
                        guardando = true
                        onConfirmar(tituloLimpio, descripcion.trim(), prioridadSeleccionada)
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