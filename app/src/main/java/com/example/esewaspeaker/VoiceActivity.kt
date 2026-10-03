package com.example.esewaspeaker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class VoiceActivity : AppCompatActivity(),
    TextToSpeech.OnInitListener {

    private lateinit var voiceStatus: TextView
    private lateinit var voiceDetails: TextView
    private lateinit var speechRateValue: TextView
    private lateinit var pitchValue: TextView
    private lateinit var speechRateSeekBar: SeekBar
    private lateinit var pitchSeekBar: SeekBar

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val preferences by lazy {
        getSharedPreferences(
            "wallet_settings",
            MODE_PRIVATE
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_voice
        )

        bindViews()
        loadSettings()
        setupListeners()
        updateLabels()
        initializeTts()
    }

    private fun bindViews() {

        voiceStatus =
            findViewById(
                R.id.voiceStatus
            )

        voiceDetails =
            findViewById(
                R.id.voiceDetails
            )

        speechRateValue =
            findViewById(
                R.id.speechRateValue
            )

        pitchValue =
            findViewById(
                R.id.pitchValue
            )

        speechRateSeekBar =
            findViewById(
                R.id.speechRateSeekBar
            )

        pitchSeekBar =
            findViewById(
                R.id.pitchSeekBar
            )
    }

    private fun initializeTts() {

        ttsReady = false

        voiceStatus.text =
            getString(
                R.string.voice_checking
            )

        voiceStatus.setTextColor(
            getColor(
                R.color.text_secondary
            )
        )

        voiceDetails.text =
            getString(
                R.string.voice_checking_details
            )

        tts =
            TextToSpeech(
                this,
                this
            )
    }

    private fun loadSettings() {

        val savedRate =
            preferences.getFloat(
                "speech_rate",
                1.0f
            )

        val savedPitch =
            preferences.getFloat(
                "speech_pitch",
                1.0f
            )

        speechRateSeekBar.progress =
            ((savedRate - 0.5f) * 20f)
                .toInt()
                .coerceIn(0, 20)

        pitchSeekBar.progress =
            ((savedPitch - 0.5f) * 20f)
                .toInt()
                .coerceIn(0, 20)
    }

    private fun setupListeners() {

        findViewById<Button>(
            R.id.backButton
        ).setOnClickListener {
            finish()
        }

        findViewById<Button>(
            R.id.testVoiceButton
        ).setOnClickListener {
            speakTest()
        }

        findViewById<Button>(
            R.id.ttsSettingsButton
        ).setOnClickListener {
            openTtsSettings()
        }

        speechRateSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val rate =
                        rateFromProgress(
                            progress
                        )

                    preferences.edit()
                        .putFloat(
                            "speech_rate",
                            rate
                        )
                        .apply()

                    updateLabels()
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            }
        )

        pitchSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val pitch =
                        pitchFromProgress(
                            progress
                        )

                    preferences.edit()
                        .putFloat(
                            "speech_pitch",
                            pitch
                        )
                        .apply()

                    updateLabels()
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
            }
        )
    }

    private fun rateFromProgress(
        progress: Int
    ): Float {

        return 0.5f +
                (progress / 20f)
    }

    private fun pitchFromProgress(
        progress: Int
    ): Float {

        return 0.5f +
                (progress / 20f)
    }

    private fun updateLabels() {

        val rate =
            rateFromProgress(
                speechRateSeekBar.progress
            )

        val pitch =
            pitchFromProgress(
                pitchSeekBar.progress
            )

        speechRateValue.text =
            if (rate == 1.0f) {

                getString(
                    R.string.normal
                )

            } else {

                String.format(
                    Locale.US,
                    "%.1fx",
                    rate
                )
            }

        pitchValue.text =
            if (pitch == 1.0f) {

                getString(
                    R.string.normal
                )

            } else {

                String.format(
                    Locale.US,
                    "%.1fx",
                    pitch
                )
            }
    }

    override fun onInit(
        status: Int
    ) {

        if (status != TextToSpeech.SUCCESS) {

            setTtsUnavailable(
                getString(
                    R.string.voice_unavailable
                ),
                getString(
                    R.string.voice_setup_required
                )
            )

            return
        }

        val result =
            tts?.setLanguage(
                Locale.forLanguageTag(
                    "ne-NP"
                )
            )

        ttsReady =
            result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED

        if (!ttsReady) {

            setTtsUnavailable(
                getString(
                    R.string.voice_unavailable
                ),
                getString(
                    R.string.nepali_tts_required
                )
            )

            return
        }

        updateReadyStatus()
    }

    private fun updateReadyStatus() {

        if (!ttsReady) {
            return
        }

        val engine =
            tts?.defaultEngine

        voiceStatus.text =
            getString(
                R.string.voice_ready
            )

        voiceStatus.setTextColor(
            getColor(
                R.color.success
            )
        )

        voiceDetails.text =
            if (!engine.isNullOrBlank()) {

                getString(
                    R.string.voice_engine_ready,
                    engine
                )

            } else {

                getString(
                    R.string.voice_ready_details
                )
            }
    }

    private fun setTtsUnavailable(
        statusText: String,
        detailText: String
    ) {

        ttsReady = false

        voiceStatus.text =
            statusText

        voiceStatus.setTextColor(
            getColor(
                R.color.danger
            )
        )

        voiceDetails.text =
            detailText
    }

    private fun speakTest() {

        if (!ttsReady) {
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
            "नमस्कार। यो Wallet Speaker को आवाज परीक्षण हो। तपाईंको खातामा पचास रुपैयाँ प्राप्त भएको छ।",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "voice-test-${System.currentTimeMillis()}"
        )
    }

    private fun openTtsSettings() {

        try {

            startActivity(
                Intent(
                    "com.android.settings.TTS_SETTINGS"
                )
            )

        } catch (_: Exception) {

            startActivity(
                Intent(
                    Settings.ACTION_SETTINGS
                )
            )
        }
    }

    override fun onResume() {

        super.onResume()

        if (ttsReady) {
            updateReadyStatus()
        }
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()

        tts = null
        ttsReady = false

        super.onDestroy()
    }
}