package com.arv.practica1.data


import com.arv.practica1.model.Noticia
import com.arv.practica1.network.NewsApiService
import kotlinx.coroutines.flow.first
import okio.Source
import org.intellij.lang.annotations.Language

class NewsRepository(
    private val apiService: NewsApiService,
    private val noticiaDao: NoticiasDao
) {
    suspend fun obtenerNoticias(
        apiKey: String,
        sources: String? = null,
        country: String? = null,
        category: String? = null,
        language: String? = null,
        q: String? = null
    ): List<Noticia> {
        return try {
            val hasSource = !sources.isNullOrBlank()
            val sourceParam = if (hasSource) sources else null
            val countryParam = if(hasSource) null else country?.ifBlank { null }
            val categoryParam = if ( hasSource) null else category?.ifBlank { null }
            val languageParam = if(hasSource) null else language?.ifBlank { null }
            val qParam= q?.ifBlank { null }
            val response = apiService.getTopHeadlines(
                apiKey = apiKey,
                sources = sourceParam,
                country = countryParam,
                category = categoryParam,
                language = languageParam,
                q = qParam

            )

            if(response.status!="ok"){
                throw Exception("Error")
            }

            val articles = response.articles?:emptyList()

            if(articles.isEmpty()){
                return emptyList()
            }


            val noticiasSanitizadas = articles.map { noticia ->
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