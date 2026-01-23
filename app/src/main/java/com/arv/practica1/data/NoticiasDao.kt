package com.arv.practica1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.arv.practica1.model.Noticia
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticiasDao{
    @Query("SELECT * FROM noticia ORDER BY publishedAt DESC")
    fun obtenerTodas(): Flow<List<Noticia>>

    @Insert
    suspend fun insertar(noticias:List<Noticia>)

    @Query("DELETE FROM noticia")
    suspend fun borrarTodas()
}