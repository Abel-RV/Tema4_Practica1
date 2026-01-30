package com.arv.practica1.data


import com.arv.practica1.model.Noticia
import com.arv.practica1.network.NewsApiService
import kotlinx.coroutines.flow.first
import okio.Source

class NewsRepository(
    private val apiService: NewsApiService,
    private val noticiaDao: NoticiasDao
) {
    suspend fun obtenerNoticias(
        apiKey: String,
        sources: String? = null,
        country: String? = null,
        category: String? = null,
        q: String? = null
    ): List<Noticia> {
        return try {
            val sourceParam = if(!sources.isNullOrBlank()){
                sources
            } else {
                null
            }

            val countryParam = if(sourceParam==null&& !country.isNullOrBlank()){
                country
            }else{
                null
            }

            val categoryParam = if(sourceParam==null&&!category.isNullOrBlank()){
                category
            }else{
                null
            }

            val qParam = if(!q.isNullOrBlank()){
                q
            }else{
                null
            }

            val response = apiService.getTopHeadlines(
                apiKey = apiKey,
                sources = sourceParam,
                country = countryParam,
                category = categoryParam,
                q = qParam

            )
            println("Llamada exitosa. Status: ${response.status}, Total: ${response.totalResults}")
            println("Primer artículo: ${response.articles.firstOrNull()?.titulo}")
            if (response.status != "ok" || response.articles.isEmpty()) {
                throw Exception("Respuesta vacía: status=${response.status}, total=${response.totalResults}")
            }
            val noticiasSanitizadas = response.articles.map { noticia ->
                noticia.copy(
                    url = noticia.url.trim(),
                    urlToImage = noticia.urlToImage?.trim()
                )
            }
            noticiaDao.borrarTodas()
            noticiaDao.insertar(noticiasSanitizadas)
            noticiasSanitizadas
        } catch (e: Exception) {
            // 7. Fallback a caché solo si hay datos guardados
            val cached = noticiaDao.obtenerTodas().first()
            if (cached.isNotEmpty()) {
                println("Usando caché tras error: ${e.message}")
                return cached
            } else {
                println("Error crítico (sin caché): ${e.message}")
                throw e
            }
        }
    }
}