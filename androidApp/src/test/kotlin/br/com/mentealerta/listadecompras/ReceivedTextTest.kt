package br.com.mentealerta.listadecompras

import android.content.ComponentName
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Receber uma lista pelo "Compartilhar" do Android (#135): o manifesto e a leitura do intent. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReceivedTextTest {
    private fun send(text: CharSequence?, type: String = "text/plain") = Intent(Intent.ACTION_SEND).apply {
        this.type = type
        if (text != null) putExtra(Intent.EXTRA_TEXT, text)
    }

    @Test
    fun theAppAppearsInTheShareSheetForText() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = send("banana").setPackage(context.packageName)

        val activities = context.packageManager.queryIntentActivities(intent, 0)

        assertEquals(
            listOf(ComponentName(context, MainActivity::class.java).className),
            activities.map { it.activityInfo.name },
        )
    }

    @Test
    fun readsTheSharedText() {
        assertEquals("- Banana\n- Maçã", send("- Banana\n- Maçã").receivedText())
    }

    @Test
    fun ignoresOtherIntents() {
        assertNull(Intent(Intent.ACTION_MAIN).receivedText())
        assertNull(send(null).receivedText())
        assertNull(send("  \n").receivedText())
        assertNull(send("banana", type = "image/png").receivedText())
    }

    @Test
    fun ignoresTheOldIntentWhenReopenedFromRecents() {
        val intent = send("banana").addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
        assertNull(intent.receivedText())
    }
}
