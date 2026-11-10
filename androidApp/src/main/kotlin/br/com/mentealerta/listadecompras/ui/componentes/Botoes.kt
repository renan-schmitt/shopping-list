package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.ui.tema.TemaQuadrinhos

/** Tamanho dos ícones dentro dos botões (protótipo: 30px). */
private val TamanhoDoIcone = 30.dp

/**
 * Botão principal: verde-folha, letra Bangers em caixa alta, ~88dp de altura
 * (plano §4.8). Ex.: "Terminei" com o texto de apoio "Faltam 5".
 *
 * O ícone, se houver, fica acima do texto, como no protótipo; é decorativo
 * para o TalkBack, que lê só o texto.
 */
@Composable
fun BotaoPrincipal(
    texto: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    icone: Painter? = null,
    textoDeApoio: String? = null,
) {
    val cores = MaterialTheme.colorScheme
    val letras = MaterialTheme.typography
    BotaoQuadrinhos(
        aoTocar = aoTocar,
        fundo = cores.primary,
        sombra = TemaQuadrinhos.tokens.sombraMedia,
        alturaMinima = TemaQuadrinhos.tokens.alturaBotaoPrincipal,
        modifier = modifier,
    ) {
        if (icone != null) IconeDoBotao(icone, cores.onPrimary)
        TextoCaixaAlta(
            texto = texto,
            estilo = letras.headlineMedium,
            cor = cores.onPrimary,
            contornoDaLetra = contornoParaLetra(cores.onPrimary, cores.outline),
        )
        if (textoDeApoio != null) {
            TextoCaixaAlta(texto = textoDeApoio, estilo = letras.labelMedium, cor = cores.onPrimary)
        }
    }
}

/**
 * Botão secundário: papel com contorno e letra verde em Lexend Deca. Ex.:
 * "Adicionar" ao lado do "Terminei", ou "Ops, voltar" no aviso.
 *
 * Altura mínima de 64dp (área de toque, plano §1.7); [alturaMinima] permite
 * igualar ao botão principal quando os dois ficam lado a lado.
 */
@Composable
fun BotaoSecundario(
    texto: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    icone: Painter? = null,
    alturaMinima: Dp = TemaQuadrinhos.tokens.areaDeToqueMinima,
) {
    val cores = MaterialTheme.colorScheme
    BotaoQuadrinhos(
        aoTocar = aoTocar,
        fundo = cores.surface,
        sombra = TemaQuadrinhos.tokens.sombraMedia,
        alturaMinima = alturaMinima,
        modifier = modifier,
    ) {
        if (icone != null) IconeDoBotao(icone, cores.primary)
        TextoCaixaAlta(texto = texto, estilo = MaterialTheme.typography.labelLarge, cor = cores.primary)
    }
}

/**
 * Base dos botões: contorno, sombra sólida e o "afundar" ao tocar no lugar da
 * onda do Material. A área de toque inclui a sombra e tem no mínimo 64dp nos
 * dois sentidos; a altura cresce se o texto quebrar a linha.
 */
@Composable
private fun BotaoQuadrinhos(
    aoTocar: () -> Unit,
    fundo: Color,
    sombra: Dp,
    alturaMinima: Dp,
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val tokens = TemaQuadrinhos.tokens
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val afundar by animateDpAsState(if (pressionado) tokens.deslocamentoAoTocar else 0.dp, label = "afundar")

    Column(
        modifier = modifier
            .clickable(interactionSource = interacao, indication = null, role = Role.Button, onClick = aoTocar)
            .estiloQuadrinhos(
                forma = MaterialTheme.shapes.medium,
                fundo = fundo,
                contorno = MaterialTheme.colorScheme.outline,
                larguraDoContorno = tokens.contornoGrosso,
                sombra = sombra,
                afundar = afundar,
            )
            .heightIn(min = alturaMinima)
            .widthIn(min = tokens.areaDeToqueMinima)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = conteudo,
    )
}

@Composable
private fun IconeDoBotao(icone: Painter, cor: Color) {
    Icon(painter = icone, contentDescription = null, tint = cor, modifier = Modifier.size(TamanhoDoIcone))
}

