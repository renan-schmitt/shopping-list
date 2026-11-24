package br.com.mentealerta.listadecompras

import android.app.Application
import br.com.mentealerta.listadecompras.billing.CurrentActivity
import br.com.mentealerta.listadecompras.di.appModule
import br.com.mentealerta.listadecompras.di.sharedModule
import br.com.mentealerta.listadecompras.premium.PremiumRepository
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShoppingListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ShoppingListApplication)
            modules(sharedModule, appModule)
        }
        registerActivityLifecycleCallbacks(get<CurrentActivity>())
        // Pergunta à loja, em silêncio, se a pessoa já tem a versão completa (celular novo, reinstalado)
        get<PremiumRepository>().start()
    }
}
