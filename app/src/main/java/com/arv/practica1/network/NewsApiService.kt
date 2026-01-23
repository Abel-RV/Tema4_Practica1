package com.arv.practica1.network



import retrofit2.http.Query
import com.arv.practica1.model.NewsResponse
import retrofit2.http.GET
import java.util.Locale

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("sources") sources: String,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}