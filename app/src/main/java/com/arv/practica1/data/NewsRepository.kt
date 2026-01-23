package com.arv.practica1.data

import com.arv.practica1.model.Noticia
import com.arv.practica1.network.NewsApiService
import kotlinx.coroutines.flow.first

class NewsRepository(
    private val apiService: NewsApiService,
    private val noticiaDao: NoticiasDao
) {
    suspend fun obtenerNoticias(apiKey: String): List<Noticia> {
        return try {
            // 1. Llamada a la API
            val response = apiService.getTopHeadlines(
                sources = "techcrunch",
                apiKey = apiKey
            )
            // 2. Depuración (opcional)
            println("Llamada exitosa. Status: ${response.status}, Total: ${response.totalResults}")
            println("Primer artículo: ${response.articles.firstOrNull()?.titulo}")
            // 3. Validar respuesta
            if (response.status != "ok" || response.articles.isEmpty()) {
                throw Exception("Respuesta vacía: status=${response.status}, total=${response.totalResults}")
            }
            // 4. Sanitizar URLs (¡clave para evitar errores en Room!)
            val noticiasSanitizadas = response.articles.map { noticia ->
                noticia.copy(
                    url = noticia.url.trim(),
                    urlToImage = noticia.urlToImage?.trim()
                )
            }
            // 5. Guardar en Room
            noticiaDao.borrarTodas()
            noticiaDao.insertar(noticiasSanitizadas)
            // 6. Devolver datos limpios
            noticiasSanitizadas
        } catch (e: Exception) {
            // 7. Fallback a caché solo si hay datos guardados
            val cached = noticiaDao.obtenerTodas().first()
            if (cached.isNotEmpty()) {
                println("Usando caché tras error: ${e.message}")
                return cached
            } else {
                println("Error crítico (sin caché): ${e.message}")
                throw e // Relanzar si no hay datos locales
            }
        }
    }
}