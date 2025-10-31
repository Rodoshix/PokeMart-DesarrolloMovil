package com.pokermart.ecommerce.data.database

import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.OpcionProductoEntity
import com.pokermart.ecommerce.data.database.entities.ProductoEntity
import com.pokermart.ecommerce.data.database.entities.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrecargaDatos {

    suspend fun cargar(baseDeDatos: PokeMartDatabase) = withContext(Dispatchers.IO) {
        insertarCategorias(baseDeDatos)
        insertarProductosYOpciones(baseDeDatos)
        insertarUsuarios(baseDeDatos)
    }

    private suspend fun insertarCategorias(baseDeDatos: PokeMartDatabase) {
        val dao = baseDeDatos.categoriaDao()
        if (dao.contar() > 0) return

        val categorias = listOf(
            CategoriaEntity(
                id = 1,
                nombre = "Medicinas",
                descripcion = "Cura a tus Pokemon despues de los combates mas duros.",
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/potion.png"
            ),
            CategoriaEntity(
                id = 2,
                nombre = "Poke Balls",
                descripcion = "Captura criaturas de todo tipo con las ultimas tecnologias.",
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/poke-ball.png"
            ),
            CategoriaEntity(
                id = 3,
                nombre = "MTs y DTs",
                descripcion = "Ensena movimientos poderosos a tu equipo favorito.",
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/tm.png"
            )
        )

        dao.insertarTodas(categorias)
    }

    private suspend fun insertarProductosYOpciones(baseDeDatos: PokeMartDatabase) {
        val productoDao = baseDeDatos.productoDao()
        val opcionDao = baseDeDatos.opcionProductoDao()
        if (productoDao.contar() > 0 || opcionDao.contar() > 0) return

        val productos = listOf(
            ProductoEntity(
                id = 100,
                categoriaId = 1,
                nombre = "Pocion",
                descripcion = "Restaura 20 PS. Ideal para entrenadores en sus primeras rutas.",
                precio = 200.0,
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/potion.png",
                destacado = true
            ),
            ProductoEntity(
                id = 101,
                categoriaId = 1,
                nombre = "Superpocion",
                descripcion = "Reponte antes de un gimnasio con una cura de 60 PS.",
                precio = 700.0,
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/super-potion.png",
                destacado = false
            ),
            ProductoEntity(
                id = 200,
                categoriaId = 2,
                nombre = "Ultra Ball",
                descripcion = "Excelente tasa de captura para Pokemon dificiles.",
                precio = 1200.0,
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/ultra-ball.png",
                destacado = true
            ),
            ProductoEntity(
                id = 201,
                categoriaId = 2,
                nombre = "Honor Ball",
                descripcion = "Una edicion especial para conmemorar capturas epicas.",
                precio = 300.0,
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/premier-ball.png",
                destacado = false
            ),
            ProductoEntity(
                id = 300,
                categoriaId = 3,
                nombre = "MT Trueno",
                descripcion = "Potente ataque electrico con chance de paralizar.",
                precio = 3000.0,
                imagenUrl = "https://assets.pokemon.com/assets/cms2/img/items/tm.png",
                destacado = true
            )
        )

        val opciones = listOf(
            OpcionProductoEntity(
                id = 1000,
                productoId = 100,
                nombre = "Paquete x1",
                descripcion = "Una sola unidad para emergencias puntuales.",
                precioExtra = 0.0,
                stock = 250
            ),
            OpcionProductoEntity(
                id = 1001,
                productoId = 100,
                nombre = "Paquete x5",
                descripcion = "Juego de cinco con ligero descuento aplicado.",
                precioExtra = -50.0,
                stock = 90
            ),
            OpcionProductoEntity(
                id = 1002,
                productoId = 101,
                nombre = "Edicion gimnasio",
                descripcion = "Incluye etiqueta premium y fragancia de baya.",
                precioExtra = 120.0,
                stock = 40
            ),
            OpcionProductoEntity(
                id = 2000,
                productoId = 200,
                nombre = "Paquete profesional",
                descripcion = "Tres Ultra Balls listas para la batalla.",
                precioExtra = 200.0,
                stock = 60
            ),
            OpcionProductoEntity(
                id = 2001,
                productoId = 201,
                nombre = "Coleccion conmemorativa",
                descripcion = "Set con display acrilico para exhibicion.",
                precioExtra = 350.0,
                stock = 15
            ),
            OpcionProductoEntity(
                id = 3000,
                productoId = 300,
                nombre = "Licencia unica",
                descripcion = "Permite ensenar Trueno a un Pokemon compatible.",
                precioExtra = 0.0,
                stock = 25
            )
        )

        productoDao.insertarTodos(productos)
        opcionDao.insertarTodas(opciones)
    }

    private suspend fun insertarUsuarios(baseDeDatos: PokeMartDatabase) {
        val dao = baseDeDatos.usuarioDao()
        if (dao.contar() > 0) return

        val usuarioDemo = UsuarioEntity(
            id = 1,
            nombre = "Entrenador Demo",
            correo = "demo@pokemart.com",
            contrasena = "pikachu123"
        )

        dao.insertar(usuarioDemo)
    }
}
