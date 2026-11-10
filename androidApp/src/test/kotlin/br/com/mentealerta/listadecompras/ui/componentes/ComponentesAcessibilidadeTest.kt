package br.com.mentealerta.listadecompras.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import br.com.mentealerta.listadecompras.R
import br.com.mentealerta.listadecompras.ui.tema.ListaDeComprasTheme
import br.com.mentealerta.listadecompras.ui.tema.TemaScreenshotTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Acessibilidade dos componentes base (plano §1.7 e §4.6): área de toque,
 * papel de botão, título de seção, texto original para o TalkBack e aviso
 * anunciado. O contraste das cores fica em [ContrasteTest].
 *
 * Os checks do Accessibility Test Framework não rodam no Robolectric (não
 * acusam nem um botão de 20dp sem rótulo), por isso as regras são conferidas
 * aqui uma a uma.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = TemaScreenshotTest.TELA)
class ComponentesAcessibilidadeTest {

    @get:Rule
    val compose = createComposeRule()

    private var toquesNoPrincipal = 0
    private var toquesNoSecundario = 0
    private var toquesNoVoltar = 0

    @Before
    fun montar() {
        compose.setContent {
            ListaDeComprasTheme(escuro = false) {
                Column {
                    TituloDeSecao("Hortifrúti")
                    BotaoPrincipal(texto = "Terminei", textoDeApoio = "Faltam 5", aoTocar = { toquesNoPrincipal++ })
                    BotaoSecundario(
                        texto = "Adicionar",
                        icone = painterResource(R.drawable.ic_adicionar),
                        aoTocar = { toquesNoSecundario++ },
                    )
                    AvisoComVoltar(
                        mensagem = "Feijão saiu da lista.",
                        textoDoBotao = "Ops, voltar",
                        aoTocarNoBotao = { toquesNoVoltar++ },
                    )
                }
            }
        }
    }

    @Test
    fun botaoPrincipalTemAlturaDe88dpEPapelDeBotao() {
        compose.onNodeWithText("Terminei", substring = true)
            .assertHeightIsAtLeast(88.dp)
            .assertWidthIsAtLeast(64.dp)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .performClick()
        assertEquals(1, toquesNoPrincipal)
    }

    @Test
    fun botoesSecundariosTemAreaDeToqueDe64dp() {
        listOf("Adicionar", "Ops, voltar").forEach { texto ->
            compose.onNodeWithText(texto)
                .assertHeightIsAtLeast(64.dp)
                .assertWidthIsAtLeast(64.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        }
        compose.onNodeWithText("Adicionar").performClick()
        compose.onNodeWithText("Ops, voltar").performClick()
        assertEquals(1, toquesNoSecundario)
        assertEquals(1, toquesNoVoltar)
    }

    @Test
    fun talkBackLeTextoEmCaixaNormal() {
        // Na tela aparece "TERMINEI", mas o TalkBack recebe o texto original.
        compose.onNodeWithText("Terminei", substring = true)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Text,
                    listOf(AnnotatedString("Terminei"), AnnotatedString("Faltam 5")),
                ),
            )
        compose.onNodeWithText("TERMINEI", substring = true).assertDoesNotExist()
    }

    @Test
    fun iconeNaoTemRotuloProprio() {
        // Só o texto é lido: o ícone acompanha, não substitui (guia, princípio 5).
        compose.onNodeWithText("Adicionar")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun tituloDeSecaoEhTitulo() {
        compose.onNodeWithText("Hortifrúti").assert(isHeading())
    }

    @Test
    fun avisoEhAnunciado() {
        compose.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite),
            useUnmergedTree = true,
        ).assertExists()
    }
}
