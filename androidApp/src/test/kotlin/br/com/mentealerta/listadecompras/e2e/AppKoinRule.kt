package br.com.mentealerta.listadecompras.e2e

import androidx.test.core.app.ApplicationProvider
import br.com.mentealerta.listadecompras.di.appModule
import br.com.mentealerta.listadecompras.di.sharedModule
import br.com.mentealerta.listadecompras.ui.add.FakeVoiceRecognizer
import br.com.mentealerta.listadecompras.voice.VoiceRecognizer
import org.junit.rules.ExternalResource
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Sobe o Koin do app de verdade (catálogo embarcado, Room, DataStore e casos de uso), trocando só o
 * reconhecimento de voz pelo [voice] falso. Fica por fora da regra que abre a activity, para o Koin
 * já estar pronto quando a tela pedir os ViewModels. O banco e as preferências vão para o diretório
 * do app, que o Robolectric cria limpo a cada teste: cada um começa com a lista vazia.
 */
class AppKoinRule : ExternalResource() {
    val voice = FakeVoiceRecognizer()

    override fun before() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(sharedModule, appModule, module { single<VoiceRecognizer> { voice } })
        }
    }

    override fun after() {
        stopKoin()
    }
}
