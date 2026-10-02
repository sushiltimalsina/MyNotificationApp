package com.example.esewaspeaker

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class WalletsActivity : AppCompatActivity(),TextToSpeech.OnInitListener {

    private lateinit var esewaNameInput: EditText
    private lateinit var khaltiNameInput: EditText

    private lateinit var esewaReceivedSwitch: Switch
    private lateinit var esewaSentSwitch: Switch
    private lateinit var khaltiReceivedSwitch: Switch
    private lateinit var khaltiSentSwitch: Switch

    private lateinit var esewaEnabledLabel: TextView
    private lateinit var khaltiEnabledLabel: TextView

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val preferences by lazy {
        getSharedPreferences("wallet_settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_wallets)

        tts = TextToSpeech(this, this)

        bindViews()
        loadSavedSettings()
        setupListeners()
        updateWalletLabels()
    }

    private fun bindViews() {

        esewaNameInput = findViewById(R.id.esewaNameInput)
        khaltiNameInput = findViewById(R.id.khaltiNameInput)

        esewaReceivedSwitch = findViewById(R.id.esewaReceivedSwitch)
        esewaSentSwitch = findViewById(R.id.esewaSentSwitch)

        khaltiReceivedSwitch = findViewById(R.id.khaltiReceivedSwitch)
        khaltiSentSwitch = findViewById(R.id.khaltiSentSwitch)

        esewaEnabledLabel = findViewById(R.id.esewaEnabledLabel)
        khaltiEnabledLabel = findViewById(R.id.khaltiEnabledLabel)
    }

    private fun loadSavedSettings() {

        esewaNameInput.setText(
            preferences.getString(
                "esewa_owner_name",
                ""
            )
        )

        khaltiNameInput.setText(
            preferences.getString(
                "khalti_owner_name",
                ""
            )
        )

        esewaReceivedSwitch.isChecked =
            preferences.getBoolean(
                "esewa_received_enabled",
                true
            )

        esewaSentSwitch.isChecked =
            preferences.getBoolean(
                "esewa_sent_enabled",
                true
            )

        khaltiReceivedSwitch.isChecked =
            preferences.getBoolean(
                "khalti_received_enabled",
                true
            )

        khaltiSentSwitch.isChecked =
            preferences.getBoolean(
                "khalti_sent_enabled",
                true
            )
    }

    private fun setupListeners() {

        findViewById<Button>(R.id.saveWalletsButton)
            .setOnClickListener {
                saveWallets()
            }

        findViewById<Button>(R.id.testEsewaButton)
            .setOnClickListener {
                testEsewa()
            }

        findViewById<Button>(R.id.testKhaltiButton)
            .setOnClickListener {
                testKhalti()
            }

        findViewById<Button>(R.id.backButton)
            .setOnClickListener {
                finish()
            }

        esewaReceivedSwitch.setOnCheckedChangeListener { _, _ ->
            updateWalletLabels()
        }

        esewaSentSwitch.setOnCheckedChangeListener { _, _ ->
            updateWalletLabels()
        }

        khaltiReceivedSwitch.setOnCheckedChangeListener { _, _ ->
            updateWalletLabels()
        }

        khaltiSentSwitch.setOnCheckedChangeListener { _, _ ->
            updateWalletLabels()
        }
    }

    private fun updateWalletLabels() {

        val esewaEnabled =
            esewaReceivedSwitch.isChecked ||
                    esewaSentSwitch.isChecked

        val khaltiEnabled =
            khaltiReceivedSwitch.isChecked ||
                    khaltiSentSwitch.isChecked

        esewaEnabledLabel.text =
            if (esewaEnabled) {
                getString(R.string.enabled)
            } else {
                getString(R.string.disabled)
            }

        esewaEnabledLabel.setTextColor(
            getColor(
                if (esewaEnabled) {
                    R.color.success
                } else {
                    R.color.text_muted
                }
            )
        )

        khaltiEnabledLabel.text =
            if (khaltiEnabled) {
                getString(R.string.enabled)
            } else {
                getString(R.string.disabled)
            }

        khaltiEnabledLabel.setTextColor(
            getColor(
                if (khaltiEnabled) {
                    R.color.success
                } else {
                    R.color.text_muted
                }
            )
        )
    }

    private fun saveWallets() {

        val esewaName =
            esewaNameInput.text.toString().trim()

        val khaltiName =
            khaltiNameInput.text.toString().trim()

        preferences.edit()
            .putString(
                "esewa_owner_name",
                esewaName
            )
            .putString(
                "khalti_owner_name",
                khaltiName
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
            .apply()

        updateWalletLabels()

        Toast.makeText(
            this,
            getString(R.string.wallet_settings_saved),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun testEsewa() {

        val name =
            esewaNameInput.text.toString().trim()

        val sentence =
            if (name.isNotBlank()) {

                "$name जी, तपाईंको ईसेवा खातामा पचास रुपैयाँ प्राप्त भएको छ।"

            } else {

                "तपाईंको ईसेवा खातामा पचास रुपैयाँ प्राप्त भएको छ।"
            }

        speak(sentence)
    }

    private fun testKhalti() {

        val name =
            khaltiNameInput.text.toString().trim()

        val sentence =
            if (name.isNotBlank()) {

                "$name जी, सुरज दाहालबाट तपाईंको खल्ती खातामा तेह्र रुपैयाँ प्राप्त भएको छ।"

            } else {

                "सुरज दाहालबाट तपाईंको खल्ती खातामा तेह्र रुपैयाँ प्राप्त भएको छ।"
            }

        speak(sentence)
    }

    private fun speak(sentence: String) {

        if (!ttsReady) {

            Toast.makeText(
                this,
                getString(R.string.tts_not_ready),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        tts?.speak(
            sentence,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "wallet-test-${System.currentTimeMillis()}"
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
        }
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }
}
