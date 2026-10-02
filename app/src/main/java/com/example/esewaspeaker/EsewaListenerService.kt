package com.example.esewaspeaker

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.concurrent.Executors

class EsewaListenerService : NotificationListenerService(),
    TextToSpeech.OnInitListener {

    companion object {

        const val TAG = "EsewaSpeaker"

        const val ESEWA_PACKAGE = "com.f1soft.esewa"
        const val KHALTI_PACKAGE = "com.khalti"

        const val PREFS = "wallet_settings"
    }

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val executor =
        Executors.newSingleThreadExecutor()

    private var lastKey = ""

    private val preferences by lazy {
        getSharedPreferences(PREFS, MODE_PRIVATE)
    }

    override fun onCreate() {

        super.onCreate()

        Log.d(TAG, "Wallet Speaker service started")

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result =
                tts?.setLanguage(Locale("ne", "NP"))

            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

            Log.d(TAG, "TTS READY: $ttsReady")

        } else {

            Log.e(TAG, "TTS initialization failed: $status")
        }
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification
    ) {

        val packageName = sbn.packageName

        if (
            packageName != ESEWA_PACKAGE &&
            packageName != KHALTI_PACKAGE
        ) {
            return
        }

        val extras: Bundle =
            sbn.notification.extras

        val title =
            extras.getCharSequence(
                Notification.EXTRA_TITLE
            )?.toString() ?: ""

        val text =
            (
                    extras.getCharSequence(
                        Notification.EXTRA_BIG_TEXT
                    )
                        ?: extras.getCharSequence(
                            Notification.EXTRA_TEXT
                        )
                    )?.toString() ?: ""

        val full =
            "$title. $text"
                .trim()

        Log.d(
            TAG,
            "Notification from: $packageName"
        )

        Log.d(
            TAG,
            "TEXT: [$full]"
        )

        if (full.isBlank()) {
            return
        }

        val key =
            "$packageName|${sbn.key}|$full"

        if (key == lastKey) {
            Log.d(TAG, "Duplicate notification ignored")
            return
        }

        lastKey = key

        executor.execute {

            val result =
                if (packageName == ESEWA_PACKAGE) {
                    buildEsewaSpeech(full)
                } else {
                    buildKhaltiSpeech(full)
                }

            if (result == null) {

                handleOtherNotification(sbn)

                return@execute
            }

            Log.d(
                TAG,
                "FINAL SPEECH: $result"
            )

            speak(result)
        }
    }

    // --------------------------------------------------
    // ESEWA
    // --------------------------------------------------

    private fun buildEsewaSpeech(
        raw: String
    ): String? {

        val lower =
            raw.lowercase()

        val amount =
            extractAmount(raw)

        val isReceived =
            listOf(
                "you have received",
                "received npr",
                "credited",
                "credit"
            ).any {
                lower.contains(it)
            }

        val isSent =
            listOf(
                "you have sent",
                "sent npr",
                "debited",
                "debit",
                "payment successful",
                "paid"
            ).any {
                lower.contains(it)
            }

        if (amount == null) {
            return null
        }

        val ownerFromNotification =
            Regex(
                """(?i)\bDear\s+([^,\n]+)"""
            )
                .find(raw)
                ?.groupValues
                ?.getOrNull(1)
                ?.trim()

        val savedOwner =
            preferences.getString(
                "esewa_owner_name",
                ""
            )?.trim()

        val owner =
            ownerFromNotification
                ?.takeIf { it.isNotBlank() }
                ?: savedOwner
                    ?.takeIf { it.isNotBlank() }

        val spokenAmount =
            NepaliNumberConverter.convert(amount)

        if (isReceived) {

            return if (owner != null) {

                "$owner जी, तपाईंको ईसेवा खातामा $spokenAmount रुपैयाँ प्राप्त भएको छ।"

            } else {

                "तपाईंको ईसेवा खातामा $spokenAmount रुपैयाँ प्राप्त भएको छ।"
            }
        }

        if (isSent) {

            return if (owner != null) {

                "$owner जी, तपाईंको ईसेवा खाताबाट $spokenAmount रुपैयाँ भुक्तानी भएको छ।"

            } else {

                "तपाईंको ईसेवा खाताबाट $spokenAmount रुपैयाँ भुक्तानी भएको छ।"
            }
        }

        return null
    }

    // --------------------------------------------------
    // KHALTI
    // --------------------------------------------------

    private fun buildKhaltiSpeech(
        raw: String
    ): String? {

        val lower =
            raw.lowercase()

        val isReceived =
            lower.contains("fund received") ||
                    lower.contains("you have received")

        val isSent =
            lower.contains("you have sent") ||
                    lower.contains("payment successful") ||
                    lower.contains("paid") ||
                    lower.contains("debited")

        val amount =
            extractAmount(raw)

        if (amount == null) {
            return null
        }

        val owner =
            preferences.getString(
                "khalti_owner_name",
                ""
            )?.trim()

        if (isReceived) {

            val sender =
                Regex(
                    """(?i)you\s+have\s+received\s+(?:rs\.?|npr\.?)\s*[\d,]+(?:\.\d+)?\s+from\s+(.+?)(?:\.|$)"""
                )
                    .find(raw)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.trim()

            val spokenAmount =
                NepaliNumberConverter.convert(amount)

            return when {

                !owner.isNullOrBlank() &&
                        !sender.isNullOrBlank() ->

                    "$owner जी, $sender बाट तपाईंको खल्ती खातामा $spokenAmount रुपैयाँ प्राप्त भएको छ।"

                !owner.isNullOrBlank() ->

                    "$owner जी, तपाईंको खल्ती खातामा $spokenAmount रुपैयाँ प्राप्त भएको छ।"

                !sender.isNullOrBlank() ->

                    "$sender बाट तपाईंको खल्ती खातामा $spokenAmount रुपैयाँ प्राप्त भएको छ।"

                else ->

                    "तपाईंको खल्ती खातामा $spokenAmount रुपैयाँ प्राप्त भएको छ।"
            }
        }

        if (isSent) {

            val spokenAmount =
                NepaliNumberConverter.convert(amount)

            return if (!owner.isNullOrBlank()) {

                "$owner जी, तपाईंको खल्ती खाताबाट $spokenAmount रुपैयाँ भुक्तानी भएको छ।"

            } else {

                "तपाईंको खल्ती खाताबाट $spokenAmount रुपैयाँ भुक्तानी भएको छ।"
            }
        }

        return null
    }

    // --------------------------------------------------
    // OTHER NOTIFICATIONS
    // --------------------------------------------------

    private fun handleOtherNotification(
        sbn: StatusBarNotification
    ) {

        val silent =
            preferences.getBoolean(
                "silent_other_notifications",
                false
            )

        if (!silent) {

            Log.d(
                TAG,
                "Non-transaction notification ignored. System handles it normally."
            )

            return
        }

        /*
         * NotificationListenerService can cancel the notification,
         * which prevents it from remaining in the notification shade.
         *
         * However, Android/MIUI may already have played the original
         * notification sound before this listener receives it.
         */
        try {

            cancelNotification(sbn.key)

            Log.d(
                TAG,
                "Non-transaction notification cancelled because silent mode is enabled."
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Could not cancel notification",
                e
            )
        }
    }

    // --------------------------------------------------
    // AMOUNT
    // --------------------------------------------------

    private fun extractAmount(
        text: String
    ): String? {

        val withCurrency =
            Regex(
                """(?:rs\.?|npr\.?|रु\.?)\s*([\d,]+(?:\.\d+)?)""",
                RegexOption.IGNORE_CASE
            )

        withCurrency.find(text)?.let {

            return it.groupValues[1]
                .replace(",", "")
        }

        return null
    }

    // --------------------------------------------------
    // SPEAK
    // --------------------------------------------------

    private fun speak(
        sentence: String
    ) {

        Log.d(
            TAG,
            "ATTEMPTING TO SPEAK: $sentence"
        )

        if (!ttsReady) {

            Log.e(
                TAG,
                "TTS NOT READY"
            )

            return
        }

        val result =
            tts?.speak(
                sentence,
                TextToSpeech.QUEUE_ADD,
                null,
                "wallet-${System.currentTimeMillis()}"
            )

        Log.d(
            TAG,
            "TTS SPEAK RESULT: $result"
        )
    }

    override fun onDestroy() {

        Log.d(
            TAG,
            "Wallet Speaker service destroyed"
        )

        tts?.stop()
        tts?.shutdown()

        executor.shutdown()

        super.onDestroy()
    }
}