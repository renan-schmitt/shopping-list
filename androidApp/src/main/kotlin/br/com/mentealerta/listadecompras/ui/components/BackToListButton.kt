package br.com.mentealerta.listadecompras.ui.components

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.mentealerta.listadecompras.R

/**
 * "Voltar para a lista", no topo das telas que abrem por cima da lista (adicionar, editar, mudar a
 * lista). A seta curva verde, ao lado do texto, gira para trás uma vez quando a tela abre (#122); o
 * movimento segue a escala de animações do celular. Na edição de um produto da conferência, o
 * [text] é "Voltar para a lista recebida" (#136).
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun BackToListButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.back_to_list),
) {
    val arrow = AnimatedImageVector.animatedVectorResource(R.drawable.avd_back_to_list)
    var played by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { played = true }
    SecondaryButton(
        text = text,
        onClick = onClick,
        icon = rememberAnimatedVectorPainter(arrow, atEnd = played),
        // A seta tem as cores dela: verde com contorno.
        tintIcon = false,
        modifier = modifier,
    )
}
