package com.arv.practica1.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Noticia(
    @SerializedName("title")
    val titulo:String,
    @PrimaryKey val url:String,
    @SerializedName("description")
    val descripcion:String?,
    val urlToImage:String?,
    val publishedAt:String
)