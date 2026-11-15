package com.AzoguePablo.gestorpersonaldetareas.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Prioridad {
    BAJA, MEDIA, ALTA
}

// Le decimos a Room que esta clase será una tabla llamada "tabla_tareas"
@Entity(tableName = "tareas")
data class Tarea(
    // PrimaryKey hace que este sea el identificador principal.
    // autoGenerate = true le dice a Room que cuente por nosotros (1, 2, 3...)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val prioridad: Prioridad,
    val completada: Boolean = false
)