package com.example.esewaspeaker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NotificationSettingsActivity :
    AppCompatActivity() {

    private lateinit var accessStatus: TextView
    private lateinit var silenceOtherSwitch: Switch

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
            R.layout.activity_notifications
        )

        accessStatus =
            findViewById(
                R.id.accessStatus
            )

        silenceOtherSwitch =
            findViewById(
                R.id.silenceOtherSwitch
            )

        findViewById<Button>(
            R.id.backButton
        ).setOnClickListener {
            finish()
        }

        findViewById<Button>(
            R.id.openNotificationSettings
        ).setOnClickListener {
            openNotificationAccessSettings()
        }

        silenceOtherSwitch.isChecked =
            preferences.getBoolean(
                "silent_other_notifications",
                false
            )

        silenceOtherSwitch
            .setOnCheckedChangeListener { _, checked ->

                preferences.edit()
                    .putBoolean(
                        "silent_other_notifications",
                        checked
                    )
                    .apply()
            }

        updateAccessStatus()
    }

    override fun onResume() {
        super.onResume()

        if (::accessStatus.isInitialized) {
            updateAccessStatus()
        }
    }

    private fun updateAccessStatus() {

        val enabledListeners =
            Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )

        val enabled =
            enabledListeners
                ?.contains(packageName) == true

        if (enabled) {

            accessStatus.text =
                getString(
                    R.string.notification_access_enabled
                )

            accessStatus.setTextColor(
                getColor(
                    R.color.success
                )
            )

        } else {

            accessStatus.text =
                getString(
                    R.string.notification_access_disabled
                )

            accessStatus.setTextColor(
                getColor(
                    R.color.danger
                )
            )
        }
    }

    private fun openNotificationAccessSettings() {

        startActivity(
            Intent(
                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
            )
        )
    }
}