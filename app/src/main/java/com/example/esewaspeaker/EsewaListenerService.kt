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

        private const val ESEWA_RECEIVED_ENABLED =
            "esewa_received_enabled"

        private const val ESEWA_SENT_ENABLED =
            "esewa_sent_enabled"

        private const val KHALTI_RECEIVED_ENABLED =
            "khalti_received_enabled"

        private const val KHALTI_SENT_ENABLED =
            "khalti_sent_enabled"

        private const val SILENT_OTHER_NOTIFICATIONS =
            "silent_other_notifications"
    }

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var listenerConnected = false

    private val executor =
        Executors.newSingleThreadExecutor()

    private var lastKey = ""

    private val preferences by lazy {
        getSharedPreferences(
            PREFS,
            MODE_PRIVATE
        )
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(
            TAG,
            "Wallet Speaker service started"
        )

        tts = TextToSpeech(
            this,
            this
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            val result =
                tts?.setLanguage(
                    Locale.forLanguageTag("ne-NP")
                )

            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

            Log.d(
                TAG,
                "TTS initialized. Ready: $ttsReady"
            )

        } else {

            Log.e(
                TAG,
                "TTS initialization failed"
            )
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        listenerConnected = true

        Log.d(
            TAG,
            "Notification listener connected."
        )
    }

    override fun onListenerDisconnected() {
        listenerConnected = false

        Log.d(
            TAG,
            "Notification listener disconnected."
        )

        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification
    ) {
        val packageName = sbn.packageName

        /*
         * Only process notifications originating
         * from eSewa or Khalti.
         */
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
            "$title. $text".trim()

        /*
         * Do not log notification contents.
         * Financial notifications may contain
         * sensitive information.
         */
        if (full.isBlank()) {
            return
        }

        /*
         * Prevent processing the exact same
         * notification repeatedly.
         */
        val key =
            "$packageName|${sbn.key}|$full"

        if (key == lastKey) {

            Log.d(
                TAG,
                "Duplicate notification ignored"
            )

            return
        }

        lastKey = key

        executor.execute {

            val result =
                if (packageName == ESEWA_PACKAGE) {
                    buildEsewaTransaction(full)
                } else {
                    buildKhaltiTransaction(full)
                }

            when (result) {

                is TransactionResult.Transaction -> {

                    saveTransaction(
                        result.record
                    )

                    if (result.enabled) {

                        speak(
                            result.sentence
                        )

                    } else {

                        Log.d(
                            TAG,
                            "Transaction announcement disabled."
                        )
                    }
                }

                TransactionResult.NotTransaction -> {

                    handleOtherNotification(
                        sbn
                    )
                }
            }
        }
    }

    private fun buildEsewaTransaction(
        raw: String
    ): TransactionResult {

        val lower =
            raw.lowercase()

        val amount =
            extractAmount(raw)

        if (amount == null) {
            return TransactionResult.NotTransaction
        }

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

        if (!isReceived && !isSent) {
            return TransactionResult.NotTransaction
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
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: savedOwner
                    ?.takeIf {
                        it.isNotBlank()
                    }

        val spokenAmount =
            NepaliNumberConverter.convert(
                amount
            )

        if (isReceived) {

            val enabled =
                preferences.getBoolean(
                    ESEWA_RECEIVED_ENABLED,
                    true
                )

            val sentence =
                if (owner != null) {

                    "$owner जी, तपाईंको ईसेवा खातामा " +
                            "$spokenAmount रुपैयाँ प्राप्त भएको छ।"

                } else {

                    "तपाईंको ईसेवा खातामा " +
                            "$spokenAmount रुपैयाँ प्राप्त भएको छ।"
                }

            return TransactionResult.Transaction(

                record = TransactionRecord(
                    wallet = "eSewa",
                    type = "received",
                    amount = amount,
                    person = null,
                    timestamp = System.currentTimeMillis()
                ),

                sentence = sentence,

                enabled = enabled
            )
        }

        if (isSent) {

            val enabled =
                preferences.getBoolean(
                    ESEWA_SENT_ENABLED,
                    true
                )

            val sentence =
                if (owner != null) {

                    "$owner जी, तपाईंको ईसेवा खाताबाट " +
                            "$spokenAmount रुपैयाँ भुक्तानी भएको छ।"

                } else {

                    "तपाईंको ईसेवा खाताबाट " +
                            "$spokenAmount रुपैयाँ भुक्तानी भएको छ।"
                }

            return TransactionResult.Transaction(

                record = TransactionRecord(
                    wallet = "eSewa",
                    type = "sent",
                    amount = amount,
                    person = null,
                    timestamp = System.currentTimeMillis()
                ),

                sentence = sentence,

                enabled = enabled
            )
        }

        return TransactionResult.NotTransaction
    }

    private fun buildKhaltiTransaction(
        raw: String
    ): TransactionResult {

        val lower =
            raw.lowercase()

        val amount =
            extractAmount(raw)

        if (amount == null) {
            return TransactionResult.NotTransaction
        }

        val isReceived =
            lower.contains("fund received") ||
                    lower.contains("you have received")

        val isSent =
            lower.contains("you have sent") ||
                    lower.contains("payment successful") ||
                    lower.contains("paid") ||
                    lower.contains("debited")

        if (!isReceived && !isSent) {
            return TransactionResult.NotTransaction
        }

        val owner =
            preferences.getString(
                "khalti_owner_name",
                ""
            )?.trim()

        if (isReceived) {

            val enabled =
                preferences.getBoolean(
                    KHALTI_RECEIVED_ENABLED,
                    true
                )

            val sender =
                Regex(
                    """(?i)you\s+have\s+received\s+(?:rs\.?|npr\.?)\s*[\d,]+(?:\.\d+)?\s+from\s+(.+?)(?:\.|$)"""
                )
                    .find(raw)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.trim()

            val spokenAmount =
                NepaliNumberConverter.convert(
                    amount
                )

            val sentence =
                when {

                    !owner.isNullOrBlank() &&
                            !sender.isNullOrBlank() -> {

                        "$owner जी, $sender बाट " +
                                "तपाईंको खल्ती खातामा " +
                                "$spokenAmount रुपैयाँ प्राप्त भएको छ।"
                    }

                    !owner.isNullOrBlank() -> {

                        "$owner जी, तपाईंको खल्ती खातामा " +
                                "$spokenAmount रुपैयाँ प्राप्त भएको छ।"
                    }

                    !sender.isNullOrBlank() -> {

                        "$sender बाट तपाईंको खल्ती खातामा " +
                                "$spokenAmount रुपैयाँ प्राप्त भएको छ।"
                    }

                    else -> {

                        "तपाईंको खल्ती खातामा " +
                                "$spokenAmount रुपैयाँ प्राप्त भएको छ।"
                    }
                }

            return TransactionResult.Transaction(

                record = TransactionRecord(
                    wallet = "Khalti",
                    type = "received",
                    amount = amount,
                    person = sender,
                    timestamp = System.currentTimeMillis()
                ),

                sentence = sentence,

                enabled = enabled
            )
        }

        if (isSent) {

            val enabled =
                preferences.getBoolean(
                    KHALTI_SENT_ENABLED,
                    true
                )

            val spokenAmount =
                NepaliNumberConverter.convert(
                    amount
                )

            val sentence =
                if (!owner.isNullOrBlank()) {

                    "$owner जी, तपाईंको खल्ती खाताबाट " +
                            "$spokenAmount रुपैयाँ भुक्तानी भएको छ।"

                } else {

                    "तपाईंको खल्ती खाताबाट " +
                            "$spokenAmount रुपैयाँ भुक्तानी भएको छ।"
                }

            return TransactionResult.Transaction(

                record = TransactionRecord(
                    wallet = "Khalti",
                    type = "sent",
                    amount = amount,
                    person = null,
                    timestamp = System.currentTimeMillis()
                ),

                sentence = sentence,

                enabled = enabled
            )
        }

        return TransactionResult.NotTransaction
    }

    private fun saveTransaction(
        record: TransactionRecord
    ) {

        TransactionHistory.add(
            this,
            record
        )

        /*
         * Do not log the actual transaction.
         */
        Log.d(
            TAG,
            "Transaction saved locally."
        )
    }

    private fun handleOtherNotification(
        sbn: StatusBarNotification
    ) {

        val silent =
            preferences.getBoolean(
                SILENT_OTHER_NOTIFICATIONS,
                false
            )

        if (!silent) {

            Log.d(
                TAG,
                "Non-transaction wallet notification ignored."
            )

            return
        }

        try {

            cancelNotification(
                sbn.key
            )

            Log.d(
                TAG,
                "Non-transaction wallet notification cancelled."
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Could not cancel notification",
                e
            )
        }
    }

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

    private fun speak(
        sentence: String
    ) {

        if (!ttsReady) {

            Log.e(
                TAG,
                "TTS is not ready."
            )

            return
        }

        val rate =
            preferences.getFloat(
                "speech_rate",
                1.0f
            )

        val pitch =
            preferences.getFloat(
                "speech_pitch",
                1.0f
            )

        tts?.setSpeechRate(
            rate
        )

        tts?.setPitch(
            pitch
        )

        tts?.speak(
            sentence,
            TextToSpeech.QUEUE_ADD,
            null,
            "wallet-${System.currentTimeMillis()}"
        )
    }

    override fun onDestroy() {

        Log.d(
            TAG,
            "Wallet Speaker service destroyed"
        )

        listenerConnected = false

        tts?.stop()
        tts?.shutdown()

        executor.shutdown()

        super.onDestroy()
    }

    private sealed class TransactionResult {

        data class Transaction(
            val record: TransactionRecord,
            val sentence: String,
            val enabled: Boolean
        ) : TransactionResult()

        data object NotTransaction :
            TransactionResult()
    }
}