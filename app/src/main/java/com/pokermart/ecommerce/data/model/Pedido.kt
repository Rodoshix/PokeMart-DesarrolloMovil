package com.pokermart.ecommerce.data.model

data class Pedido(
    val id: Long = 0,
    val usuarioId: Long,
    val direccionId: Long?,
    val subtotal: Double,
    val impuesto: Double,
    val envio: Double,
    val servicio: Double,
    val total: Double,
    val metodoPago: String,
    val metodoEntrega: String,
    val creadoEl: Long = System.currentTimeMillis(),
    val items: List<PedidoItem> = emptyList()
)

data class PedidoItem(
    val id: Long = 0,
    val pedidoId: Long,
    val productoId: Long,
    val opcionId: Long?,
    val cantidad: Int,
    val precioUnitario: Double,
    val precioTotal: Double
)
