package com.AzoguePablo.gestorpersonaldetareas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.AzoguePablo.gestorpersonaldetareas.model.Prioridad
import com.AzoguePablo.gestorpersonaldetareas.model.Tarea

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioTarea(
    onGuardar: (Tarea) -> Unit, // Función que ejecutaremos al guardar
    onCancelar: () -> Unit      // Función que ejecutaremos al cancelar
) {
    // 'remember' guarda el valor; 'mutableStateOf' avisa a Compose si el valor cambia para redibujar
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf(Prioridad.MEDIA) }

    // Estado para controlar si mostramos un error en el título
    var errorTitulo by remember { mutableStateOf<String?>(null) }

    // Estados para el menú desplegable de prioridad
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text(text = "Nueva Tarea", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Título
        OutlinedTextField(
            value = titulo,
            onValueChange = {
                titulo = it
                errorTitulo = null // Limpiamos el error si el usuario vuelve a escribir
            },
            label = { Text("Título") },
            isError = errorTitulo != null,
            supportingText = { if (errorTitulo != null) Text(errorTitulo!!) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de Descripción
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Menú desplegable para Prioridad
        ExposedDropdownMenuBox(
            expanded = expandido,
            onExpandedChange = { expandido = it }
        ) {
            OutlinedTextField(
                value = prioridad.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Prioridad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false }
            ) {
                Prioridad.values().forEach { prio ->
                    DropdownMenuItem(
                        text = { Text(prio.name) },
                        onClick = {
                            prioridad = prio
                            expandido = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botones
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onCancelar) {
                Text("Cancelar")
            }
            Button(onClick = {
                // Validaciones antes de guardar
                val tituloLimpio = titulo.trim()
                if (tituloLimpio.isEmpty()) {
                    errorTitulo = "El título no puede estar vacío"
                } else if (tituloLimpio.length < 3) {
                    errorTitulo = "Debe tener al menos 3 caracteres"
                } else {
                    // Si todo es correcto, creamos la tarea y llamamos a onGuardar
                    val nuevaTarea = Tarea(
                        titulo = tituloLimpio,
                        descripcion = descripcion.trim(),
                        prioridad = prioridad
                    )
                    onGuardar(nuevaTarea)
                }
            }) {
                Text("Guardar")
            }
        }
    }
}
