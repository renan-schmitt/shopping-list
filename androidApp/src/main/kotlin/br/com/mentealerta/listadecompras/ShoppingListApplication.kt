package br.com.mentealerta.listadecompras

import android.app.Application
import br.com.mentealerta.listadecompras.di.appModule
import br.com.mentealerta.listadecompras.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShoppingListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ShoppingListApplication)
            modules(sharedModule, appModule)
        }
    }
}
