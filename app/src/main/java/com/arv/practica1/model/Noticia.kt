package com.arv.practica1.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Noticia(
    val titulo:String,
    @PrimaryKey val url:String,
    val descripcion:String?,
    val urlToImage:String?,
    val publishedAt:String
)