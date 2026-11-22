package br.com.mentealerta.listadecompras.buildlogic.catalog

private const val QUEUE = "own-icons-queue.csv"
private val QUEUE_COLUMNS = listOf("icon_id", "name", "priority", "products", "notes")
private val ICON_ID_PATTERN = Regex("[a-z][a-z0-9_]*")
private val PRIORITIES = 1..3
private const val ALREADY_DRAWN = "já existe em own-icons/; ligue os produtos na planilha e tire da fila"

/** Um desenho da fila do complemento próprio: um ícone que ainda falta e os produtos que ele vai cobrir. */
data class QueuedIcon(
    /** `id` do futuro `own-icons/<id>.svg`. */
    val id: String,
    val name: String,
    /** 1 = compra de toda semana ou todo mês; 2 = compra comum; 3 = ocasional ou regional. */
    val priority: Int,
    val productIds: List<String>,
    val notes: String,
)

/** Resultado da leitura da fila: os desenhos e os erros encontrados. */
data class IconQueue(val icons: List<QueuedIcon>, val errors: List<String>)

/**
 * Lê e confere `catalog/own-icons-queue.csv` contra o [catalog] já validado e os [ownIcons] existentes.
 *
 * Um desenho já feito sai da fila, e um produto com ícone fiel não precisa dela; os dois casos são erro,
 * para a fila não ficar velha.
 */
fun parseIconQueue(csv: String, catalog: Catalog, ownIcons: Set<String>): IconQueue {
    val table = parseCsv(QUEUE, csv, QUEUE_COLUMNS)
    val errors = table.errors.toMutableList()
    val products = catalog.products.associateBy { it.id }
    val seenIcons = mutableSetOf<String>()
    val seenProducts = mutableSetOf<String>()
    val icons = table.rows.mapNotNull { row ->
        val at = "$QUEUE:${row.line}"
        val id = row["icon_id"]
        val priority = row["priority"].toIntOrNull()
        val productIds = row["products"].split(';').map { it.trim() }.filter { it.isNotEmpty() }
        val rowErrors = buildList {
            if (!ICON_ID_PATTERN.matches(id)) add("$at: icon_id \"$id\" fora do padrão (minúsculas, números e _)")
            if (!seenIcons.add(id)) add("$at: icon_id \"$id\" repetido")
            if (id in ownIcons) add("$at: \"$id\" $ALREADY_DRAWN")
            if (row["name"].isEmpty()) add("$at: name vazio")
            if (priority == null || priority !in PRIORITIES) add("$at: priority deve ser 1, 2 ou 3")
            if (productIds.isEmpty()) add("$at: products vazio")
            productIds.forEach { productId ->
                val product = products[productId]
                when {
                    product == null -> add("$at: produto \"$productId\" não existe ou não está ativo")
                    product.iconFit == "exact" -> add("$at: \"$productId\" já tem ícone fiel; tire da fila")
                    !seenProducts.add(productId) -> add("$at: produto \"$productId\" já está em outro desenho")
                }
            }
        }
        errors += rowErrors
        if (rowErrors.isEmpty() && priority != null) {
            QueuedIcon(id, row["name"], priority, productIds, row["notes"])
        } else {
            null
        }
    }
    return IconQueue(icons, errors)
}
