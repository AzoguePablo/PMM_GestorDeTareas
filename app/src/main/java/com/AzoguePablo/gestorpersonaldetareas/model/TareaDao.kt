package com.AzoguePablo.gestorpersonaldetareas.model


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas ORDER BY id DESC")
    fun obtenerTodasLasTareas(): Flow<List<Tarea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTarea(tarea: Tarea)

    @Delete
    suspend fun borrarTarea(tarea: Tarea)
}