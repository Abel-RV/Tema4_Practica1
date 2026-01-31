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
            // Convertir strings vacíos o solo espacios en null
            val cleanSources = sources?.trim()?.takeIf { it.isNotEmpty() }
            val cleanCountry = country?.trim()?.takeIf { it.isNotEmpty() }
            val cleanCategory = category?.trim()?.takeIf { it.isNotEmpty() }
            val cleanLanguage = language?.trim()?.takeIf { it.isNotEmpty() }
            val cleanQ = q?.trim()?.takeIf { it.isNotEmpty() }

            // Si se usa sources, country, category y language deben ser null
            val hasSource = cleanSources != null
            val sourceParam = cleanSources
            val countryParam = if (hasSource) null else cleanCountry
            val categoryParam = if (hasSource) null else cleanCategory
            val languageParam = if (hasSource) null else cleanLanguage

            println("DEBUG - Parámetros de búsqueda:")
            println("  sources: $sourceParam")
            println("  country: $countryParam")
            println("  category: $categoryParam")
            println("  language: $languageParam")
            println("  q: $cleanQ")

            val response = apiService.getTopHeadlines(
                apiKey = apiKey,
                sources = sourceParam,
                country = countryParam,
                category = categoryParam,
                language = languageParam,
                q = cleanQ
            )

            if(response.status!="ok"){
                val errorMsg = response.message ?: "Error desconocido"
                println("ERROR API - code: ${response.code}, message: $errorMsg")
                throw Exception("Error de API: $errorMsg")
            }

            val articles = response.articles?:emptyList()

            if(articles.isEmpty()){
                println("ADVERTENCIA - No se encontraron artículos")
                return emptyList()
            }

            println("ÉXITO - Se encontraron ${articles.size} artículos")

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
            // Fallback a caché solo si hay datos guardados
            println("EXCEPCIÓN - ${e.message}")
            val cached = noticiaDao.obtenerTodas().first()
            if (cached.isNotEmpty()) {
                println("Usando caché tras error: ${e.message}",
                    return cached)
            } else {
                println("Error crítico (sin caché): ${e.message}")
                throw e
            }
        }
    }
}