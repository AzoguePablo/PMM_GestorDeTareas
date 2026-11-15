package com.AzoguePablo.gestorpersonaldetareas.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tarea::class], version = 1, exportSchema = false)
abstract class TareaDatabase : RoomDatabase() {

    abstract fun tareaDao(): TareaDao

    companion object {
        // @Volatile asegura que el valor de INSTANCE siempre esté actualizado
        // y sea visible inmediatamente para todos los hilos de ejecución.
        @Volatile
        private var INSTANCE: TareaDatabase? = null

        fun getDatabase(context: Context): TareaDatabase {
            // Si INSTANCE no es nula, la devuelve directamente.
            // Si es nula, entra al bloque synchronized para crearla una sola vez.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TareaDatabase::class.java,
                    "tareas-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}