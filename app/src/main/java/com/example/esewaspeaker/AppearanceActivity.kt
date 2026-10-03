package com.example.esewaspeaker

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class AppearanceActivity : AppCompatActivity() {

    private lateinit var themeGroup: RadioGroup
    private lateinit var languageGroup: RadioGroup

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

        themeGroup = findViewById(R.id.themeGroup)
        languageGroup = findViewById(R.id.languageGroup)

        findViewById<Button>(
            R.id.backButton
        ).setOnClickListener {
            finish()
        }

        loadTheme()
        loadLanguage()

        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.systemTheme -> {
                    saveTheme("system")
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }
                R.id.lightTheme -> {
                    saveTheme("light")
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
                R.id.darkTheme -> {
                    saveTheme("dark")
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                    )
                }
            }
        }

        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.englishLang -> {
                    setAppLanguage("en")
                }
                R.id.nepaliLang -> {
                    setAppLanguage("ne")
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
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
            "dark" ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
            else ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
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

    private fun loadLanguage() {
        val lang = preferences.getString("language", "en")
        if (lang == "ne") {
            findViewById<RadioButton>(R.id.nepaliLang).isChecked = true
        } else {
            findViewById<RadioButton>(R.id.englishLang).isChecked = true
        }
    }

    private fun saveTheme(theme: String) {
        preferences.edit().putString("theme", theme).apply()
    }

    private fun setAppLanguage(tag: String) {
        preferences.edit().putString("language", tag).apply()
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(tag)
        )
    }
}
