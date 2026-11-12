package br.com.mentealerta.listadecompras.domain

/**
 * Quantidade opcional de um produto ("2 leites", "meio quilo de carne moída"). Só dados: quem monta
 * o texto ("2 leites", "1 kg") é a UI.
 */
data class Quantity(val amount: Double, val unit: QuantityUnit = QuantityUnit.UNIT) {
    init {
        require(amount > 0 && amount.isFinite()) { "A quantidade precisa ser maior que zero: $amount" }
    }
}

/** Unidades reconhecidas (plano §1.2). [UNIT] é a contagem simples: "2 leites" = 2 × [UNIT]. */
enum class QuantityUnit {
    UNIT,
    KILOGRAM,
    GRAM,
    LITER,
    DOZEN,
    HALF_DOZEN,
    PACKAGE,
    BOX,
    CAN,
    BOTTLE,
    BUNCH,
    TRAY,
    JAR,
}
