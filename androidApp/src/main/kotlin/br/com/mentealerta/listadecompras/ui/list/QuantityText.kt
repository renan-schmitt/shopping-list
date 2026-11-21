package br.com.mentealerta.listadecompras.ui.list

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.domain.Quantity
import br.com.mentealerta.listadecompras.domain.QuantityUnit
import java.text.NumberFormat
import java.util.Locale

/** Quantidade pronta para mostrar ([visible], ex.: "½ kg") e para o TalkBack ([spoken], ex.: "meio quilo"). */
data class QuantityText(val visible: String, val spoken: String)

private const val HALF = 0.5
private const val MAX_FRACTION_DIGITS = 3

/**
 * Monta o texto da quantidade (plano §1.1): contagem simples só com o número ("2"), demais
 * unidades com o nome ("1 kg", "2 litros", "meia dúzia"). Decimais seguem o idioma ("1,5 kg") e
 * meia unidade aparece como "½", que o TalkBack lê como "meio"/"meia".
 */
fun Quantity.format(resources: Resources, locale: Locale = resources.configuration.locales[0]): QuantityText {
    if (unit == QuantityUnit.HALF_DOZEN && amount == 1.0) {
        return resources.getString(R.string.quantity_one_half_dozen).let { QuantityText(it, it) }
    }
    val number = if (amount == HALF) {
        resources.getString(R.string.quantity_half)
    } else {
        NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = MAX_FRACTION_DIGITS }.format(amount)
    }
    // Plural pela parte inteira, como em português: 0,5 e 1,5 ficam no singular ("½ litro", "1,5 litro").
    // Abaixo de 1 conta como 1, porque a regra do idioma do aparelho (ex.: inglês) põe o 0 no plural.
    val visible = resources.getQuantityString(unit.pluralsRes, amount.toInt().coerceAtLeast(1), number)
    val spoken = unit.halfRes?.takeIf { amount == HALF }?.let(resources::getString) ?: visible
    return QuantityText(visible, spoken)
}

@get:PluralsRes
private val QuantityUnit.pluralsRes: Int
    get() = when (this) {
        QuantityUnit.UNIT -> R.plurals.quantity_unit
        QuantityUnit.KILOGRAM -> R.plurals.quantity_kilogram
        QuantityUnit.GRAM -> R.plurals.quantity_gram
        QuantityUnit.LITER -> R.plurals.quantity_liter
        QuantityUnit.DOZEN -> R.plurals.quantity_dozen
        QuantityUnit.HALF_DOZEN -> R.plurals.quantity_half_dozen
        QuantityUnit.PACKAGE -> R.plurals.quantity_package
        QuantityUnit.BOX -> R.plurals.quantity_box
        QuantityUnit.CAN -> R.plurals.quantity_can
        QuantityUnit.BOTTLE -> R.plurals.quantity_bottle
        QuantityUnit.BUNCH -> R.plurals.quantity_bunch
        QuantityUnit.TRAY -> R.plurals.quantity_tray
        QuantityUnit.JAR -> R.plurals.quantity_jar
    }

/** Como o TalkBack lê meia unidade; `null` quando não há forma natural (meia "meia dúzia"). */
@get:StringRes
private val QuantityUnit.halfRes: Int?
    get() = when (this) {
        QuantityUnit.UNIT -> R.string.quantity_half_unit
        QuantityUnit.KILOGRAM -> R.string.quantity_half_kilogram
        QuantityUnit.GRAM -> R.string.quantity_half_gram
        QuantityUnit.LITER -> R.string.quantity_half_liter
        QuantityUnit.DOZEN -> R.string.quantity_half_of_dozen
        QuantityUnit.HALF_DOZEN -> null
        QuantityUnit.PACKAGE -> R.string.quantity_half_package
        QuantityUnit.BOX -> R.string.quantity_half_box
        QuantityUnit.CAN -> R.string.quantity_half_can
        QuantityUnit.BOTTLE -> R.string.quantity_half_bottle
        QuantityUnit.BUNCH -> R.string.quantity_half_bunch
        QuantityUnit.TRAY -> R.string.quantity_half_tray
        QuantityUnit.JAR -> R.string.quantity_half_jar
    }
