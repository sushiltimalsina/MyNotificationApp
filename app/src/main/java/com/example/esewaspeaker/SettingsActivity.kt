package com.example.esewaspeaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_settings
        )

        findViewById<Button>(
            R.id.backButton
        ).setOnClickListener {
            finish()
        }

        findViewById<Button>(
            R.id.walletsButton
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    WalletsActivity::class.java
                )
            )
        }

        findViewById<Button>(
            R.id.announcementsButton
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AnnouncementsActivity::class.java
                )
            )
        }

        findViewById<Button>(
            R.id.voiceButton
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    VoiceActivity::class.java
                )
            )
        }

        findViewById<Button>(
            R.id.notificationsButton
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    NotificationSettingsActivity::class.java
                )
            )
        }

        findViewById<Button>(
            R.id.appearanceButton
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AppearanceActivity::class.java
                )
            )
        }

        findViewById<Button>(
            R.id.aboutButton
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AboutActivity::class.java
                )
            )
        }
    }
}