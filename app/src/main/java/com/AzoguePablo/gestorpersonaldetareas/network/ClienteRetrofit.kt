package com.AzoguePablo.gestorpersonaldetareas.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. Definimos las operaciones que podemos hacer contra el servidor
interface ServicioTareasApi {
    // El enunciado pide usar: GET /todos?userId=1
    @GET("todos")
    suspend fun obtenerTareas(
        @Query("userId") userId: Int = 1
    ): List<TareaApi>
}

// 2. Creamos el objeto único (Singleton) que gestiona la conexión a Internet
object ClienteRetrofit {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    val api: ServicioTareasApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON -> Kotlin
            .build()
            .create(ServicioTareasApi::class.java)
    }
}