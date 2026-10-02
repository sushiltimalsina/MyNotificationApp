package com.example.esewaspeaker

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AnnouncementsActivity : AppCompatActivity() {

    private lateinit var esewaReceivedSwitch: Switch
    private lateinit var esewaSentSwitch: Switch
    private lateinit var khaltiReceivedSwitch: Switch
    private lateinit var khaltiSentSwitch: Switch
    private lateinit var statusText: TextView

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
            R.layout.activity_announcements
        )

        bindViews()
        loadSettings()
        setupListeners()
        updateStatus()
    }

    private fun bindViews() {

        esewaReceivedSwitch =
            findViewById(
                R.id.esewaReceivedSwitch
            )

        esewaSentSwitch =
            findViewById(
                R.id.esewaSentSwitch
            )

        khaltiReceivedSwitch =
            findViewById(
                R.id.khaltiReceivedSwitch
            )

        khaltiSentSwitch =
            findViewById(
                R.id.khaltiSentSwitch
            )

        statusText =
            findViewById(
                R.id.statusText
            )
    }

    private fun loadSettings() {

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

        findViewById<Button>(
            R.id.backButton
        ).setOnClickListener {
            finish()
        }

        esewaReceivedSwitch
            .setOnCheckedChangeListener { _, checked ->
                preferences.edit()
                    .putBoolean(
                        "esewa_received_enabled",
                        checked
                    )
                    .apply()

                updateStatus()
            }

        esewaSentSwitch
            .setOnCheckedChangeListener { _, checked ->
                preferences.edit()
                    .putBoolean(
                        "esewa_sent_enabled",
                        checked
                    )
                    .apply()

                updateStatus()
            }

        khaltiReceivedSwitch
            .setOnCheckedChangeListener { _, checked ->
                preferences.edit()
                    .putBoolean(
                        "khalti_received_enabled",
                        checked
                    )
                    .apply()

                updateStatus()
            }

        khaltiSentSwitch
            .setOnCheckedChangeListener { _, checked ->
                preferences.edit()
                    .putBoolean(
                        "khalti_sent_enabled",
                        checked
                    )
                    .apply()

                updateStatus()
            }
    }

    private fun updateStatus() {

        val enabled =
            esewaReceivedSwitch.isChecked ||
                    esewaSentSwitch.isChecked ||
                    khaltiReceivedSwitch.isChecked ||
                    khaltiSentSwitch.isChecked

        if (enabled) {

            statusText.text =
                getString(
                    R.string.announcement_enabled
                )

            statusText.setTextColor(
                getColor(
                    R.color.success
                )
            )

        } else {

            statusText.text =
                getString(
                    R.string.announcement_disabled
                )

            statusText.setTextColor(
                getColor(
                    R.color.text_muted
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (
            ::esewaReceivedSwitch.isInitialized
        ) {
            loadSettings()
            updateStatus()
        }
    }
}