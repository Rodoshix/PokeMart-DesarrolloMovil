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
                imagenUrl = "https://static.wikia.nocookie.net/espokemon/images/9/95/Poci%C3%B3n_%28Dream_World%29.png/revision/latest?cb=20110130140443",
                destacado = true
            ),
            ProductoEntity(
                id = 101,
                categoriaId = 1,
                nombre = "Superpocion",
                descripcion = "Reponte antes de un gimnasio con una cura de 60 PS.",
                precio = 700.0,
                imagenUrl = "https://static.wikia.nocookie.net/espokemon/images/b/b9/Superpoci%C3%B3n_%28Dream_World%29.png/revision/latest?cb=20110130141027",
                destacado = false
            ),
            ProductoEntity(
                id = 200,
                categoriaId = 2,
                nombre = "Ultra Ball",
                descripcion = "Excelente tasa de captura para Pokemon dificiles.",
                precio = 1200.0,
                imagenUrl = "https://static.wikia.nocookie.net/espokemon/images/c/c9/Ultra_Ball_%28Ilustraci%C3%B3n%29.png/revision/latest?cb=20090125150713",
                destacado = true
            ),
            ProductoEntity(
                id = 201,
                categoriaId = 2,
                nombre = "Honor Ball",
                descripcion = "Una edicion especial para conmemorar capturas epicas.",
                precio = 300.0,
                imagenUrl = "https://static.wikia.nocookie.net/espokemon/images/a/a0/Honor_Ball_%28Ilustraci%C3%B3n%29.png/revision/latest?cb=20090125151053",
                destacado = false
            ),
            ProductoEntity(
                id = 300,
                categoriaId = 3,
                nombre = "MT Trueno",
                descripcion = "Potente ataque electrico con chance de paralizar.",
                precio = 3000.0,
                imagenUrl = "data:image/png;base64,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",
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

        val usuarios = listOf(
            UsuarioEntity(
                id = 1,
                nombre = "Entrenador Demo",
                correo = "demo@pokemart.com",
                contrasena = "pikachu123"
            ),
            UsuarioEntity(
                id = 2,
                nombre = "Ash",
                apellido = "Ketchum",
                correo = "ash@pokemart.com",
                contrasena = "pikachu123",
                run = "12.345.678-5",
                fechaNacimiento = "1990-05-22",
                region = "Kanto",
                comuna = "Pueblo Paleta",
                direccion = "Ruta 1 sin numero"
            ),
            UsuarioEntity(
                id = 3,
                nombre = "Misty",
                apellido = "Waterflower",
                correo = "misty@gmail.com",
                contrasena = "togepi123",
                run = "16.789.345-7",
                fechaNacimiento = "1992-08-12",
                region = "Kanto",
                comuna = "Ciudad Celeste",
                direccion = "Avenida Cascada 456"
            )
        )

        usuarios.forEach { dao.insertar(it) }
    }
}
