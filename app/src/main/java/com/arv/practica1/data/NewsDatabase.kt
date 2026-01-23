package com.arv.practica1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arv.practica1.model.Noticia

@Database(entities = [Noticia::class], version = 1, exportSchema = false)
abstract class NewsDatabase: RoomDatabase(){
    abstract fun noticiasDao(): NoticiasDao
    companion object{
        @Volatile
        private var INSTANCE: NewsDatabase?=null

        fun getDatabase(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "noticias.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}