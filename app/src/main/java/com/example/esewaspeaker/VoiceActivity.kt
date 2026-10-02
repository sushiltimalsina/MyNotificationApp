package com.example.esewaspeaker

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class VoiceActivity : AppCompatActivity(),
    TextToSpeech.OnInitListener {

    private lateinit var voiceStatus: TextView
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

        tts = TextToSpeech(
            this,
            this
        )

        loadSettings()
        setupListeners()
        updateLabels()
    }

    private fun bindViews() {

        voiceStatus =
            findViewById(
                R.id.voiceStatus
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

        speechRateSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val rate =
                        rateFromProgress(progress)

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
                        pitchFromProgress(progress)

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
                getString(R.string.normal)
            } else {
                String.format(
                    Locale.US,
                    "%.1fx",
                    rate
                )
            }

        pitchValue.text =
            if (pitch == 1.0f) {
                getString(R.string.normal)
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

            ttsReady = false

            voiceStatus.text =
                getString(
                    R.string.voice_unavailable
                )

            voiceStatus.setTextColor(
                getColor(
                    R.color.danger
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

        if (ttsReady) {

            voiceStatus.text =
                getString(
                    R.string.voice_ready
                )

            voiceStatus.setTextColor(
                getColor(
                    R.color.success
                )
            )

        } else {

            voiceStatus.text =
                getString(
                    R.string.voice_unavailable
                )

            voiceStatus.setTextColor(
                getColor(
                    R.color.danger
                )
            )
        }
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

        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)

        tts?.speak(
            "नमस्कार। यो Wallet Speaker को आवाज परीक्षण हो। तपाईंको खातामा पचास रुपैयाँ प्राप्त भएको छ।",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "voice-test-${System.currentTimeMillis()}"
        )
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }
}