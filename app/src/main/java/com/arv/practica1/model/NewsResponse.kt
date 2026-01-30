package com.arv.practica1.model

data class NewsResponse(
    val status:String,
    val totalResults:Int?,
    val articles: List<Noticia>?,
    val code: String?,
    val message: String?
)