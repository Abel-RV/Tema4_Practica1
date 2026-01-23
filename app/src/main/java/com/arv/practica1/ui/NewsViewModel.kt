package com.arv.practica1.ui

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arv.practica1.data.NewsDatabase
import com.arv.practica1.data.NewsRepository
import com.arv.practica1.model.Noticia
import com.arv.practica1.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface EstadoNoticias{
    object Cargando: EstadoNoticias
    data class Exito(val noticias: List<Noticia>): EstadoNoticias
    data class Error(val mensaje:String): EstadoNoticias
}

class NewsViewModel(application: Application): AndroidViewModel(application){
    private val db = NewsDatabase.getDatabase(application)
    private val repository = NewsRepository(RetrofitClient.apiService,db.noticiasDao())

    private val _estado = MutableStateFlow<EstadoNoticias>(EstadoNoticias.Cargando)
    val estado: StateFlow<EstadoNoticias> = _estado

    fun cargarNoticas(apiKey: String){
        viewModelScope.launch {
            _estado.value= EstadoNoticias.Cargando
            try{
                val noticias = repository.obtenerNoticias(apiKey)
                _estado.value= EstadoNoticias.Exito(noticias)
            }catch (e: Exception){
                _estado.value= EstadoNoticias.Error("Error: ${e.message ?: "Desconocido"}")
            }
        }
    }
}