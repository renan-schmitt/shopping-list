package br.com.mentealerta.listadecompras.billing

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * A tela que está na frente, para abrir a janela de pagamento sobre ela. Guarda só uma referência
 * fraca: a tela fechada não fica presa na memória.
 */
class CurrentActivity : Application.ActivityLifecycleCallbacks {
    @Volatile
    private var resumed: WeakReference<Activity>? = null

    fun get(): Activity? = resumed?.get()

    override fun onActivityResumed(activity: Activity) {
        resumed = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (resumed?.get() === activity) resumed = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
