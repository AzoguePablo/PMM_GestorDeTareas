package com.AzoguePablo.gestorpersonaldetareas.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.AzoguePablo.gestorpersonaldetareas.model.Tarea
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas ORDER BY id DESC")
    fun obtenerTodasLasTareas(): Flow<List<Tarea>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarTarea(tarea: Tarea)

    @Update
    suspend fun actualizarTarea(tarea: Tarea)
    @Delete
    suspend fun borrarTarea(tarea: Tarea)
}