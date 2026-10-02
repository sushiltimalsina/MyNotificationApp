package com.example.esewaspeaker

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate


class AppearanceActivity : AppCompatActivity() {

    private lateinit var themeGroup: RadioGroup

    private val preferences by lazy {
        getSharedPreferences(
            "wallet_settings",
            MODE_PRIVATE
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        applySavedTheme()

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_appearance
        )

        themeGroup =
            findViewById(
                R.id.themeGroup
            )

        findViewById<Button>(
            R.id.backButton
        ).setOnClickListener {
            finish()
        }

        loadTheme()

        themeGroup.setOnCheckedChangeListener {
                _,
                checkedId ->

            when (checkedId) {

                R.id.systemTheme -> {
                    saveTheme("system")

                    AppCompatDelegate
                        .setDefaultNightMode(
                            AppCompatDelegate
                                .MODE_NIGHT_FOLLOW_SYSTEM
                        )
                }

                R.id.lightTheme -> {
                    saveTheme("light")

                    AppCompatDelegate
                        .setDefaultNightMode(
                            AppCompatDelegate
                                .MODE_NIGHT_NO
                        )
                }

                R.id.darkTheme -> {
                    saveTheme("dark")

                    AppCompatDelegate
                        .setDefaultNightMode(
                            AppCompatDelegate
                                .MODE_NIGHT_YES
                        )
                }
            }
        }
    }

    private fun applySavedTheme() {

        when (
            preferences.getString(
                "theme",
                "system"
            )
        ) {

            "light" ->
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_NO
                    )

            "dark" ->
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_YES
                    )

            else ->
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_FOLLOW_SYSTEM
                    )
        }
    }

    private fun loadTheme() {

        when (
            preferences.getString(
                "theme",
                "system"
            )
        ) {

            "light" ->
                findViewById<RadioButton>(
                    R.id.lightTheme
                ).isChecked = true

            "dark" ->
                findViewById<RadioButton>(
                    R.id.darkTheme
                ).isChecked = true

            else ->
                findViewById<RadioButton>(
                    R.id.systemTheme
                ).isChecked = true
        }
    }

    private fun saveTheme(
        theme: String
    ) {

        preferences
            .edit()
            .putString(
                "theme",
                theme
            )
            .apply()
    }
}