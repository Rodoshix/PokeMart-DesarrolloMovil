package com.pokermart.ecommerce.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pokermart.ecommerce.data.dao.CategoriaDao
import com.pokermart.ecommerce.data.dao.DireccionDao
import com.pokermart.ecommerce.data.dao.OpcionProductoDao
import com.pokermart.ecommerce.data.dao.ProductoDao
import com.pokermart.ecommerce.data.dao.UsuarioDao
import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.DireccionEntity
import com.pokermart.ecommerce.data.database.entities.OpcionProductoEntity
import com.pokermart.ecommerce.data.database.entities.ProductoEntity
import com.pokermart.ecommerce.data.database.entities.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoriaEntity::class,
        ProductoEntity::class,
        OpcionProductoEntity::class,
        UsuarioEntity::class,
        DireccionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PokeMartDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun opcionProductoDao(): OpcionProductoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun direccionDao(): DireccionDao

    companion object {
        private const val NOMBRE_BD = "poke_mart.db"
        @Volatile
        private var instancia: PokeMartDatabase? = null

        fun obtenerInstancia(contexto: Context, alcance: CoroutineScope): PokeMartDatabase {
            return instancia ?: synchronized(this) {
                val nuevaInstancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    PokeMartDatabase::class.java,
                    NOMBRE_BD
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(CallbackPreCarga(alcance))
                    .build()
                instancia = nuevaInstancia
                nuevaInstancia
            }
        }

        private class CallbackPreCarga(
            private val alcance: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                instancia?.let { baseDeDatos ->
                    alcance.launch {
                        PrecargaDatos.cargar(baseDeDatos)
                    }
                }
            }
        }
    }
}
