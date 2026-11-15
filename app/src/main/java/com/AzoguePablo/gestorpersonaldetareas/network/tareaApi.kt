package com.AzoguePablo.gestorpersonaldetareas.network

import com.google.gson.annotations.SerializedName

// Esta clase representa la estructura exacta del JSON que devuelve el servidor
data class TareaApi(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val titulo: String,
    @SerializedName("completed") val completada: Boolean
)