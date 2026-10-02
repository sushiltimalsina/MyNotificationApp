package com.example.esewaspeaker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var preferences: android.content.SharedPreferences

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private lateinit var esewaNameInput: EditText
    private lateinit var khaltiNameInput: EditText

    private lateinit var esewaReceivedSwitch: Switch
    private lateinit var esewaSentSwitch: Switch
    private lateinit var khaltiReceivedSwitch: Switch
    private lateinit var khaltiSentSwitch: Switch

    private lateinit var silentOtherSwitch: Switch

    private lateinit var serviceStatus: TextView
    private lateinit var accessStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences("wallet_settings", MODE_PRIVATE)

        tts = TextToSpeech(this, this)

        buildUI()
    }

    override fun onResume() {
        super.onResume()

        if (::accessStatus.isInitialized) {
            updateNotificationAccessStatus()
        }
    }

    private fun buildUI() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF6F7FB.toInt())
        }

        val scrollView = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 40)
        }

        // ---------------- HEADER ----------------

        val title = TextView(this).apply {
            text = "Wallet Speaker"
            textSize = 30f
            setTextColor(0xFF111827.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = "Hear your wallet transactions in Nepali"
            textSize = 15f
            setTextColor(0xFF6B7280.toInt())
            setPadding(0, 6, 0, 24)
        }

        content.addView(title)
        content.addView(subtitle)

        // ---------------- SERVICE CARD ----------------

        val serviceCard = createCard()

        serviceStatus = TextView(this).apply {
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }

        val serviceDescription = TextView(this).apply {
            text = "Wallet Speaker listens for eSewa and Khalti transaction notifications and announces them using Nepali speech."
            textSize = 14f
            setTextColor(0xFF6B7280.toInt())
        }

        serviceCard.addView(serviceStatus)
        serviceCard.addView(serviceDescription)

        content.addView(serviceCard)

        // ---------------- ACCESS CARD ----------------

        val accessCard = createCard()

        val accessTitle = sectionTitle("Notification Access")

        accessStatus = TextView(this).apply {
            textSize = 15f
            setPadding(0, 8, 0, 12)
        }

        val accessButton = Button(this).apply {
            text = "Manage Notification Access"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                )
            }
        }

        accessCard.addView(accessTitle)
        accessCard.addView(accessStatus)
        accessCard.addView(accessButton)

        content.addView(accessCard)

        // ---------------- WALLET ACCOUNTS ----------------

        val walletTitle = sectionTitle("Wallet Accounts")
        content.addView(walletTitle)

        // eSewa
        val esewaCard = createCard()

        val esewaTitle = TextView(this).apply {
            text = "eSewa"
            textSize = 21f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
        }

        val esewaDescription = TextView(this).apply {
            text = "Account holder name"
            textSize = 13f
            setTextColor(0xFF6B7280.toInt())
            setPadding(0, 4, 0, 4)
        }

        esewaNameInput = EditText(this).apply {
            hint = "Example: Saraswoti"
            setSingleLine(true)
            setText(
                preferences.getString("esewa_owner_name", "")
            )
        }

        val esewaTestButton = Button(this).apply {
            text = "🔊 Test eSewa Announcement"
            setOnClickListener {
                if (ttsReady) {
                    val name = esewaNameInput.text.toString().trim()

                    val speech =
                        if (name.isNotEmpty()) {
                            "$name जी, तपाईंको ईसेवा खातामा पचास रुपैयाँ प्राप्त भएको छ।"
                        } else {
                            "तपाईंको ईसेवा खातामा पचास रुपैयाँ प्राप्त भएको छ।"
                        }

                    speak(speech)
                }
            }
        }

        esewaCard.addView(esewaTitle)
        esewaCard.addView(esewaDescription)
        esewaCard.addView(esewaNameInput)
        esewaCard.addView(esewaTestButton)

        content.addView(esewaCard)

        // Khalti
        val khaltiCard = createCard()

        val khaltiTitle = TextView(this).apply {
            text = "Khalti"
            textSize = 21f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
        }

        val khaltiDescription = TextView(this).apply {
            text = "Account holder name"
            textSize = 13f
            setTextColor(0xFF6B7280.toInt())
            setPadding(0, 4, 0, 4)
        }

        khaltiNameInput = EditText(this).apply {
            hint = "Example: Sushil"
            setSingleLine(true)
            setText(
                preferences.getString("khalti_owner_name", "")
            )
        }

        val khaltiTestButton = Button(this).apply {
            text = "🔊 Test Khalti Announcement"
            setOnClickListener {
                if (ttsReady) {
                    val owner = khaltiNameInput.text.toString().trim()

                    val speech =
                        if (owner.isNotEmpty()) {
                            "$owner जी, सुरज दाहालबाट तपाईंको खल्ती खातामा तेह्र रुपैयाँ प्राप्त भएको छ।"
                        } else {
                            "सुरज दाहालबाट तपाईंको खल्ती खातामा तेह्र रुपैयाँ प्राप्त भएको छ।"
                        }

                    speak(speech)
                }
            }
        }

        khaltiCard.addView(khaltiTitle)
        khaltiCard.addView(khaltiDescription)
        khaltiCard.addView(khaltiNameInput)
        khaltiCard.addView(khaltiTestButton)

        content.addView(khaltiCard)

        // ---------------- SAVE BUTTON ----------------

        val saveButton = Button(this).apply {
            text = "Save Wallet Settings"
            setOnClickListener {
                saveSettings()

                Toast.makeText(
                    this@MainActivity,
                    "Wallet settings saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        content.addView(saveButton)

        // ---------------- TRANSACTIONS ----------------

        val transactionTitle = sectionTitle("Transaction Announcements")
        content.addView(transactionTitle)

        val transactionCard = createCard()

        esewaReceivedSwitch = createSwitch(
            "eSewa money received",
            true
        )

        esewaSentSwitch = createSwitch(
            "eSewa money sent",
            true
        )

        khaltiReceivedSwitch = createSwitch(
            "Khalti money received",
            true
        )

        khaltiSentSwitch = createSwitch(
            "Khalti money sent",
            true
        )

        transactionCard.addView(esewaReceivedSwitch)
        transactionCard.addView(esewaSentSwitch)
        transactionCard.addView(khaltiReceivedSwitch)
        transactionCard.addView(khaltiSentSwitch)

        content.addView(transactionCard)

        // ---------------- OTHER NOTIFICATIONS ----------------

        val otherTitle = sectionTitle("Other Notifications")
        content.addView(otherTitle)

        val otherCard = createCard()

        val otherDescription = TextView(this).apply {
            text = "Promotions and non-transaction notifications are never spoken by Wallet Speaker."
            textSize = 14f
            setTextColor(0xFF6B7280.toInt())
            setPadding(0, 0, 0, 12)
        }

        silentOtherSwitch = createSwitch(
            "Silence other wallet notifications",
            false
        )

        otherCard.addView(otherDescription)
        otherCard.addView(silentOtherSwitch)

        content.addView(otherCard)

        // ---------------- VOICE ----------------

        val voiceTitle = sectionTitle("Voice")
        content.addView(voiceTitle)

        val voiceCard = createCard()

        val voiceStatus = TextView(this).apply {
            text = "Nepali voice: checking..."
            textSize = 15f
            setPadding(0, 0, 0, 8)
        }

        val testVoiceButton = Button(this).apply {
            text = "🔊 Test Nepali Voice"
            setOnClickListener {

                if (ttsReady) {
                    speak(
                        "नमस्ते, यो वालेट स्पिकरको नेपाली आवाज परीक्षण हो।"
                    )
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Nepali TTS voice is not available",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        val ttsSettingsButton = Button(this).apply {
            text = "Open TTS Settings"
            setOnClickListener {
                try {
                    startActivity(
                        Intent("com.android.settings.TTS_SETTINGS")
                    )
                } catch (e: Exception) {
                    startActivity(
                        Intent(Settings.ACTION_SETTINGS)
                    )
                }
            }
        }

        voiceCard.addView(voiceStatus)
        voiceCard.addView(testVoiceButton)
        voiceCard.addView(ttsSettingsButton)

        content.addView(voiceCard)

        // ---------------- ABOUT ----------------

        val about = TextView(this).apply {
            text = "\nWallet Speaker\nVersion 1.0\n\nSupports eSewa and Khalti transaction announcements."
            textSize = 13f
            setTextColor(0xFF6B7280.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 20)
        }

        content.addView(about)

        scrollView.addView(content)
        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)

        loadSettings()
        updateNotificationAccessStatus()
        updateServiceStatus()
    }

    // --------------------------------------------------
    // UI HELPERS
    // --------------------------------------------------

    private fun createCard(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFFFFFFFF.toInt())

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, 20)

            layoutParams = params
        }
    }

    private fun sectionTitle(text: String): TextView {

        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF111827.toInt())
            setPadding(0, 8, 0, 14)
        }
    }

    private fun createSwitch(
        text: String,
        defaultValue: Boolean
    ): Switch {

        return Switch(this).apply {
            this.text = text
            textSize = 15f
            isChecked = defaultValue
            setPadding(0, 6, 0, 6)
        }
    }

    // --------------------------------------------------
    // SETTINGS
    // --------------------------------------------------

    private fun saveSettings() {

        preferences.edit()
            .putString(
                "esewa_owner_name",
                esewaNameInput.text.toString().trim()
            )
            .putString(
                "khalti_owner_name",
                khaltiNameInput.text.toString().trim()
            )
            .putBoolean(
                "esewa_received_enabled",
                esewaReceivedSwitch.isChecked
            )
            .putBoolean(
                "esewa_sent_enabled",
                esewaSentSwitch.isChecked
            )
            .putBoolean(
                "khalti_received_enabled",
                khaltiReceivedSwitch.isChecked
            )
            .putBoolean(
                "khalti_sent_enabled",
                khaltiSentSwitch.isChecked
            )
            .putBoolean(
                "silent_other_notifications",
                silentOtherSwitch.isChecked
            )
            .apply()
    }

    private fun loadSettings() {

        esewaReceivedSwitch.isChecked =
            preferences.getBoolean("esewa_received_enabled", true)

        esewaSentSwitch.isChecked =
            preferences.getBoolean("esewa_sent_enabled", true)

        khaltiReceivedSwitch.isChecked =
            preferences.getBoolean("khalti_received_enabled", true)

        khaltiSentSwitch.isChecked =
            preferences.getBoolean("khalti_sent_enabled", true)

        silentOtherSwitch.isChecked =
            preferences.getBoolean("silent_other_notifications", false)
    }

    // --------------------------------------------------
    // NOTIFICATION ACCESS
    // --------------------------------------------------

    private fun updateNotificationAccessStatus() {

        val enabledListeners =
            Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )

        val enabled =
            enabledListeners?.contains(packageName) == true

        accessStatus.text =
            if (enabled) {
                "● Access enabled\n\nWallet Speaker can receive wallet notifications."
            } else {
                "○ Access disabled\n\nEnable notification access so Wallet Speaker can detect transactions."
            }

        accessStatus.setTextColor(
            if (enabled) {
                0xFF15803D.toInt()
            } else {
                0xFFB45309.toInt()
            }
        )
    }

    private fun updateServiceStatus() {

        serviceStatus.text = "● Wallet Speaker Ready"
        serviceStatus.setTextColor(0xFF15803D.toInt())
    }

    // --------------------------------------------------
    // TTS
    // --------------------------------------------------

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result =
                tts?.setLanguage(Locale("ne", "NP"))

            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun speak(sentence: String) {

        if (!ttsReady) return

        tts?.speak(
            sentence,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "wallet-speaker-${System.currentTimeMillis()}"
        )
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }
}