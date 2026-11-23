package br.com.mentealerta.listadecompras.ui

import android.graphics.Paint
import android.text.Layout
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.components.ICON_ONLY_BUTTON_TAG
import br.com.mentealerta.listadecompras.ui.components.IconOnlyButton

/** Área de toque mínima do app (plano §1.7). */
val MinTouchTarget = 64.dp

/**
 * Regras de acessibilidade conferidas em qualquer tela (plano §1.7), no lugar dos checks do
 * Accessibility Test Framework, que não rodam no Robolectric:
 *
 * - todo elemento tocável tem nome para o TalkBack e pelo menos [MinTouchTarget] nos dois lados;
 * - nenhum texto cortado ou com reticências, nem palavra partida sem hífen (nomes longos quebram
 *   a linha);
 * - só campo de texto tem descrição própria: o ícone acompanha o nome, não o substitui (guia,
 *   princípio 5). A exceção é o [IconOnlyButton] (lixeira de "Adicionados", 2026-11-23).
 *
 * Devolve a lista de problemas, vazia se a tela passa.
 */
fun SemanticsNodeInteractionsProvider.accessibilityProblems(): List<String> =
    // Árvore unida: o que o TalkBack percorre, um item por foco. Sem unir: cada texto e imagem.
    onRoot().fetchSemanticsNode().descendants().flatMap { it.touchProblems() }.toList() +
        onRoot(useUnmergedTree = true).fetchSemanticsNode().descendants().flatMap { it.contentProblems() }

private fun SemanticsNode.touchProblems(): List<String> {
    if (SemanticsActions.OnClick !in config) return emptyList()
    val name = spokenName()
    val width = with(layoutInfo.density) { layoutInfo.width.toDp() }
    val height = with(layoutInfo.density) { layoutInfo.height.toDp() }
    return listOfNotNull(
        "Tocável sem nome: ${describe()}".takeIf { name.isBlank() },
        "Área de toque ${width.rounded()} × ${height.rounded()} menor que $MinTouchTarget: \"$name\""
            .takeIf { width < MinTouchTarget || height < MinTouchTarget },
    )
}

private fun SemanticsNode.contentProblems(): List<String> {
    val description = config.getOrNull(SemanticsProperties.ContentDescription)
    val layout = textLayout()
    val text = layout?.layoutInput?.text?.text
    return listOfNotNull(
        "Descrição própria fora de campo de texto: $description"
            .takeIf {
                description != null && SemanticsProperties.EditableText !in config &&
                    config.getOrNull(SemanticsProperties.TestTag) != ICON_ONLY_BUTTON_TAG
            },
        "Texto cortado: \"$text\"".takeIf { layout?.isCut() == true },
        "Palavra partida sem hífen: \"$text\"".takeIf { layout?.hasBrokenWord() == true },
    )
}

/** Reticências ou linhas além do limite. */
private fun TextLayoutResult.isCut(): Boolean = didOverflowHeight || (0 until lineCount).any { isLineEllipsized(it) }

/**
 * Linha que termina entre duas letras sem hífen. `didOverflowWidth` não serve para isso: o texto
 * encolhe até a largura dele e acusaria tudo.
 */
private fun TextLayoutResult.hasBrokenWord(): Boolean {
    val text = layoutInput.text.text
    return (0 until lineCount - 1).any { line ->
        val end = getLineEnd(line)
        end in 1 until text.length &&
            text[end - 1].isLetterOrDigit() &&
            text[end].isLetterOrDigit() &&
            !hasHyphenAt(line)
    }
}

/**
 * O hífen da quebra é desenhado, mas não entra no texto nem na medida da linha: só o
 * `android.text.Layout` sabe dele. O Compose não expõe esse layout, por isso a reflexão.
 */
private fun TextLayoutResult.hasHyphenAt(line: Int): Boolean {
    val infos = multiParagraph.field("paragraphInfoList") as List<*>
    val info = infos.first {
        (it!!.field("startLineIndex") as Int) <= line && line < (it.field("endLineIndex") as Int)
    }!!
    val textLayout = info.field("paragraph")!!.field("layout")!!
    val layout = textLayout.javaClass.getMethod("getLayout").invoke(textLayout) as Layout
    // `getEndHyphenEdit` é oculto no SDK, mas existe no aparelho (e no Robolectric).
    val getEndHyphenEdit = generateSequence<Class<*>>(layout.javaClass) { it.superclass }
        .firstNotNullOf { type -> type.declaredMethods.firstOrNull { it.name == "getEndHyphenEdit" } }
        .apply { isAccessible = true }
    val edit = getEndHyphenEdit.invoke(layout, line - info.field("startLineIndex") as Int) as Int
    return edit != Paint.END_HYPHEN_EDIT_NO_EDIT
}

private fun Any.field(name: String): Any? = javaClass.getDeclaredField(name).apply { isAccessible = true }.get(this)

private fun SemanticsNode.descendants(): Sequence<SemanticsNode> =
    sequenceOf(this) + children.asSequence().flatMap { it.descendants() }

private fun SemanticsNode.spokenName(): String = buildList {
    config.getOrNull(SemanticsProperties.ContentDescription)?.let(::addAll)
    config.getOrNull(SemanticsProperties.Text)?.forEach { add(it.text) }
    config.getOrNull(SemanticsProperties.EditableText)?.let { add(it.text) }
}.joinToString(" ")

private fun SemanticsNode.textLayout(): TextLayoutResult? {
    val action = config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: return null
    val results = mutableListOf<TextLayoutResult>()
    action(results)
    return results.firstOrNull()
}

private fun SemanticsNode.describe(): String = config.joinToString { "${it.key.name}=${it.value}" }

private fun Dp.rounded(): String = "${value.toInt()}dp"
