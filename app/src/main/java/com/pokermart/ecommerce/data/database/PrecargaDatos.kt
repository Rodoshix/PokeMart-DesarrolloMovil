package com.pokermart.ecommerce.data.database

import com.pokermart.ecommerce.data.database.entities.CategoriaEntity
import com.pokermart.ecommerce.data.database.entities.DireccionEntity
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
        insertarDirecciones(baseDeDatos)
    }

    private suspend fun insertarCategorias(baseDeDatos: PokeMartDatabase) {
        val dao = baseDeDatos.categoriaDao()
        if (dao.contar() > 0) return

        val categorias = listOf(
            CategoriaEntity(
                id = 1,
                nombre = "Medicinas",
                descripcion = "Cura a tus Pokemon despues de los combates mas duros.",
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/4/40/latest/20230115181348/Restaurar_todo_EP.png"
            ),
            CategoriaEntity(
                id = 2,
                nombre = "Poke Balls",
                descripcion = "Captura criaturas de todo tipo con las ultimas tecnologias.",
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/0/02/latest/20231226202856/Pok%C3%A9_Ball_%28Ilustraci%C3%B3n%29.png"
            ),
            CategoriaEntity(
                id = 3,
                nombre = "MTs y DTs",
                descripcion = "Ensena movimientos poderosos a tu equipo favorito.",
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/8/86/latest/20230115172652/MT_tipo_lucha_EP.png"
            )
        )

        dao.insertarTodas(categorias)
    }

    private suspend fun insertarProductosYOpciones(baseDeDatos: PokeMartDatabase) {
        val productoDao = baseDeDatos.productoDao()
        val opcionDao = baseDeDatos.opcionProductoDao()
        if (productoDao.contar() > 0 || opcionDao.contar() > 0) return

        val productos = listOf(
            // ---------- CATEGORIA 1: MEDICINAS ----------
            ProductoEntity(
                id = 100,
                categoriaId = 1,
                nombre = "Pocion",
                descripcion = "Restaura 20 PS. Ideal para entrenadores en sus primeras rutas.",
                precio = 200.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/f/fd/latest/20230115173615/Poci%C3%B3n_EP.png",
                destacado = true
            ),
            ProductoEntity(
                id = 101,
                categoriaId = 1,
                nombre = "Superpocion",
                descripcion = "Reponte antes de un gimnasio con una cura de 60 PS.",
                precio = 700.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/1/1a/latest/20230115173819/Superpoci%C3%B3n_EP.png",
                destacado = false
            ),
            ProductoEntity(
                id = 102,
                categoriaId = 1,
                nombre = "Hiperpocion",
                descripcion = "Cura potente para peleas exigentes.",
                precio = 1200.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/7/76/latest/20230115173900/Hiperpoci%C3%B3n_EP.png",
                destacado = false
            ),
            ProductoEntity(
                id = 103,
                categoriaId = 1,
                nombre = "Restaurar Todo",
                descripcion = "Restaura PS y cura todos los estados.",
                precio = 2500.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/4/40/latest/20230115181348/Restaurar_todo_EP.png",
                destacado = true
            ),
            ProductoEntity(
                id = 104,
                categoriaId = 1,
                nombre = "Revivir",
                descripcion = "Revive a un Pokemon debilitado con la mitad de PS.",
                precio = 1500.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/6/6e/latest/20230123181418/Revivir_EP.png",
                destacado = false
            ),
            // ---------- CATEGORIA 2: POKE BALLS ----------
            ProductoEntity(
                id = 200,
                categoriaId = 2,
                nombre = "Ultra Ball",
                descripcion = "Excelente tasa de captura para Pokemon dificiles.",
                precio = 1200.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/c/c9/latest/20231226203124/Ultra_Ball_%28Ilustraci%C3%B3n%29.png",
                destacado = true
            ),
            ProductoEntity(
                id = 201,
                categoriaId = 2,
                nombre = "Honor Ball",
                descripcion = "Una edicion especial para conmemorar capturas epicas.",
                precio = 300.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/a/a0/latest/20231226200646/Honor_Ball_%28Ilustraci%C3%B3n%29.png",
                destacado = false
            ),
            ProductoEntity(
                id = 202,
                categoriaId = 2,
                nombre = "Poké Ball",
                descripcion = "Poké Ball el objeto más emblemático del mundo Pokémon.",
                precio = 200.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/0/02/latest/20231226202856/Pok%C3%A9_Ball_%28Ilustraci%C3%B3n%29.png",
                destacado = false
            ),
            ProductoEntity(
                id = 203,
                categoriaId = 2,
                nombre = "Super Ball",
                descripcion = "Mejor tasa de captura que la Poke Ball.",
                precio = 600.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/5/57/latest/20231226203004/Super_Ball_%28Ilustraci%C3%B3n%29.png",
                destacado = false
            ),
            ProductoEntity(
                id = 204,
                categoriaId = 2,
                nombre = "Quick Ball",
                descripcion = "Eficaz al inicio del combate.",
                precio = 1000.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/7/73/latest/20231226201526/Veloz_Ball_%28Ilustraci%C3%B3n%29.png",
                destacado = false
            ),
            ProductoEntity(
                id = 205,
                categoriaId = 2,
                nombre = "Dusk Ball",
                descripcion = "Mas efectiva de noche o en cuevas.",
                precio = 1000.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/a/ad/latest/20231226201410/Ocaso_Ball_%28Ilustraci%C3%B3n%29.png",
                destacado = false
            ),
            ProductoEntity(
                id = 206,
                categoriaId = 2,
                nombre = "Luxury Ball",
                descripcion = "Mayor amistad para el Pokemon capturado.",
                precio = 2000.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/f/f1/latest/20231116171354/Lujo_Ball_%28Ilustraci%C3%B3n%29.png",
                destacado = true
            ),
            // ---------- CATEGORIA 3: MTs y DTs ----------
            ProductoEntity(
                id = 300,
                categoriaId = 3,
                nombre = "MT Trueno",
                descripcion = "Potente ataque electrico con chance de paralizar.",
                precio = 3000.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/0/0f/latest/20230115172106/MT_tipo_el%C3%A9ctrico_EP.png",
                destacado = true
            ),
            ProductoEntity(
                id = 301,
                categoriaId = 3,
                nombre = "MT Llamarada",
                descripcion = "Potentisimo ataque de tipo fuego.",
                precio = 3000.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/6/63/latest/20230115172030/MT_tipo_fuego_EP.png",
                destacado = false
            ),
            ProductoEntity(
                id = 302,
                categoriaId = 3,
                nombre = "MT Rayo Hielo",
                descripcion = "Ataque de hielo con posibilidad de congelar.",
                precio = 3000.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/1/13/latest/20230115172204/MT_tipo_hielo_EP.png",
                destacado = false
            ),
            ProductoEntity(
                id = 303,
                categoriaId = 3,
                nombre = "MT Terremoto",
                descripcion = "Ataque terrestre muy poderoso.",
                precio = 3500.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/4/4a/latest/20230115172719/MT_tipo_tierra_EP.png",
                destacado = true
            ),
            ProductoEntity(
                id = 304,
                categoriaId = 3,
                nombre = "MT Psiquico",
                descripcion = "Ataque psiquico de alta precision.",
                precio = 3200.0,
                imagenUrl = "https://images.wikidexcdn.net/mwuploads/wikidex/c/c9/latest/20230115172234/MT_tipo_ps%C3%ADquico_EP.png",
                destacado = false
            )
        )

        val opciones = listOf(
            // ===== Medicinas (x1, x5, x15) =====
            OpcionProductoEntity(id = 1100, productoId = 100, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 250),
            OpcionProductoEntity(id = 1101, productoId = 100, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -50.0, stock = 120),
            OpcionProductoEntity(id = 1102, productoId = 100, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -300.0, stock = 60),

            OpcionProductoEntity(id = 1110, productoId = 101, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 180),
            OpcionProductoEntity(id = 1111, productoId = 101, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -175.0, stock = 90),
            OpcionProductoEntity(id = 1112, productoId = 101, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -1050.0, stock = 45),

            OpcionProductoEntity(id = 1120, productoId = 102, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 160),
            OpcionProductoEntity(id = 1121, productoId = 102, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -300.0, stock = 75),
            OpcionProductoEntity(id = 1122, productoId = 102, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -1800.0, stock = 35),

            OpcionProductoEntity(id = 1130, productoId = 103, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 140),
            OpcionProductoEntity(id = 1131, productoId = 103, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -625.0, stock = 70),
            OpcionProductoEntity(id = 1132, productoId = 103, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -3750.0,stock = 30),

            OpcionProductoEntity(id = 1140, productoId = 104, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 150),
            OpcionProductoEntity(id = 1141, productoId = 104, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -375.0, stock = 70),
            OpcionProductoEntity(id = 1142, productoId = 104, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -2250.0, stock = 28),

            // ===== Poké Balls (x1, x5, x15) =====
            OpcionProductoEntity(id = 1200, productoId = 200, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 300),
            OpcionProductoEntity(id = 1201, productoId = 200, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -300.0, stock = 140),
            OpcionProductoEntity(id = 1202, productoId = 200, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -1800.0, stock = 70),

            OpcionProductoEntity(id = 1210, productoId = 201, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 180),
            OpcionProductoEntity(id = 1211, productoId = 201, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -75.0,  stock = 90),
            OpcionProductoEntity(id = 1212, productoId = 201, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -450.0, stock = 45),

            OpcionProductoEntity(id = 1220, productoId = 202, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 400),
            OpcionProductoEntity(id = 1221, productoId = 202, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -50.0,  stock = 180),
            OpcionProductoEntity(id = 1222, productoId = 202, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -300.0, stock = 90),

            OpcionProductoEntity(id = 1230, productoId = 203, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 260),
            OpcionProductoEntity(id = 1231, productoId = 203, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -150.0,  stock = 120),
            OpcionProductoEntity(id = 1232, productoId = 203, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -900.0, stock = 60),

            OpcionProductoEntity(id = 1240, productoId = 204, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 220),
            OpcionProductoEntity(id = 1241, productoId = 204, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -250.0,  stock = 110),
            OpcionProductoEntity(id = 1242, productoId = 204, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -1500.0, stock = 55),

            OpcionProductoEntity(id = 1250, productoId = 205, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 210),
            OpcionProductoEntity(id = 1251, productoId = 205, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -250.0,  stock = 100),
            OpcionProductoEntity(id = 1252, productoId = 205, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -1500.0, stock = 50),

            OpcionProductoEntity(id = 1260, productoId = 206, nombre = "Paquete x1",  descripcion = "Una unidad.",                   precioExtra = 0.0,    stock = 160),
            OpcionProductoEntity(id = 1261, productoId = 206, nombre = "Paquete x5",  descripcion = "Cinco unidades (descuento).",   precioExtra = -500.0, stock = 80),
            OpcionProductoEntity(id = 1262, productoId = 206, nombre = "Paquete x15", descripcion = "Quince unidades (ahorro).",     precioExtra = -3000.0, stock = 40),

            // ===== MTs (base, rapida, premium) =====
            OpcionProductoEntity(id = 1300, productoId = 300, nombre = "Version base",    descripcion = "Aprendizaje estandar.",     precioExtra = 0.0,    stock = 40),
            OpcionProductoEntity(id = 1301, productoId = 300, nombre = "Version rapida",  descripcion = "Aprendizaje acelerado.",    precioExtra = 500.0,  stock = 30),
            OpcionProductoEntity(id = 1302, productoId = 300, nombre = "Version premium", descripcion = "Incluye tutor y manual.",   precioExtra = 1200.0, stock = 20),

            OpcionProductoEntity(id = 1310, productoId = 301, nombre = "Version base",    descripcion = "Aprendizaje estandar.",     precioExtra = 0.0,    stock = 40),
            OpcionProductoEntity(id = 1311, productoId = 301, nombre = "Version rapida",  descripcion = "Aprendizaje acelerado.",    precioExtra = 500.0,  stock = 30),
            OpcionProductoEntity(id = 1312, productoId = 301, nombre = "Version premium", descripcion = "Incluye tutor y manual.",   precioExtra = 1200.0, stock = 20),

            OpcionProductoEntity(id = 1320, productoId = 302, nombre = "Version base",    descripcion = "Aprendizaje estandar.",     precioExtra = 0.0,    stock = 40),
            OpcionProductoEntity(id = 1321, productoId = 302, nombre = "Version rapida",  descripcion = "Aprendizaje acelerado.",    precioExtra = 500.0,  stock = 30),
            OpcionProductoEntity(id = 1322, productoId = 302, nombre = "Version premium", descripcion = "Incluye tutor y manual.",   precioExtra = 1200.0, stock = 20),

            OpcionProductoEntity(id = 1330, productoId = 303, nombre = "Version base",    descripcion = "Aprendizaje estandar.",     precioExtra = 0.0,    stock = 40),
            OpcionProductoEntity(id = 1331, productoId = 303, nombre = "Version rapida",  descripcion = "Aprendizaje acelerado.",    precioExtra = 600.0,  stock = 28),
            OpcionProductoEntity(id = 1332, productoId = 303, nombre = "Version premium", descripcion = "Incluye tutor y manual.",   precioExtra = 1400.0, stock = 18),

            OpcionProductoEntity(id = 1340, productoId = 304, nombre = "Version base",    descripcion = "Aprendizaje estandar.",     precioExtra = 0.0,    stock = 40),
            OpcionProductoEntity(id = 1341, productoId = 304, nombre = "Version rapida",  descripcion = "Aprendizaje acelerado.",    precioExtra = 500.0,  stock = 30),
            OpcionProductoEntity(id = 1342, productoId = 304, nombre = "Version premium", descripcion = "Incluye tutor y manual.",   precioExtra = 1200.0, stock = 20)
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

    private suspend fun insertarDirecciones(baseDeDatos: PokeMartDatabase) {
        val direccionDao = baseDeDatos.direccionDao()
        if (direccionDao.contarTotal() > 0) return

        val direcciones = listOf(
            DireccionEntity(
                usuarioId = 2,
                etiqueta = "Casa",
                addressLine = "Ruta 1 sin numero",
                referencia = "Frente al laboratorio del Profesor Oak",
                latitud = null,
                longitud = null,
                isDefault = true
            ),
            DireccionEntity(
                usuarioId = 3,
                etiqueta = "Gimnasio",
                addressLine = "Avenida Cascada 456",
                referencia = "Gimnasio de Ciudad Celeste",
                latitud = null,
                longitud = null,
                isDefault = true
            )
        )

        direcciones.forEach { direccionDao.guardar(it) }
    }
}
