package com.pokermart.ecommerce.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pokermart.ecommerce.data.dao.CategoriaDao
import com.pokermart.ecommerce.data.dao.CarritoDao
import com.pokermart.ecommerce.data.dao.DireccionDao
import com.pokermart.ecommerce.data.dao.OpcionProductoDao
import com.pokermart.ecommerce.data.dao.PedidoDao
import com.pokermart.ecommerce.data.dao.ProductoDao
import com.pokermart.ecommerce.data.dao.UsuarioDao
import com.pokermart.ecommerce.data.database.entities.CarritoItemEntity
import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.DireccionEntity
import com.pokermart.ecommerce.data.database.entities.OpcionProductoEntity
import com.pokermart.ecommerce.data.database.entities.PedidoEntity
import com.pokermart.ecommerce.data.database.entities.PedidoItemEntity
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
        DireccionEntity::class,
        CarritoItemEntity::class,
        PedidoEntity::class,
        PedidoItemEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class PokeMartDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun opcionProductoDao(): OpcionProductoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun direccionDao(): DireccionDao
    abstract fun carritoDao(): CarritoDao
    abstract fun pedidoDao(): PedidoDao

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
                    .build()
                instancia = nuevaInstancia
                alcance.launch {
                    PrecargaDatos.cargar(nuevaInstancia)
                }
                nuevaInstancia
            }
        }
    }
}
