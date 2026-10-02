package com.example.esewaspeaker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(),
    TextToSpeech.OnInitListener {

    private lateinit var preferences:
            android.content.SharedPreferences

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private lateinit var esewaOwner: TextView
    private lateinit var khaltiOwner: TextView
    private lateinit var esewaStatus: TextView
    private lateinit var khaltiStatus: TextView

    private lateinit var recentActivityContainer:
            LinearLayout

    private lateinit var emptyActivityCard:
            LinearLayout

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        /*
         * Load the saved appearance before
         * displaying the dashboard.
         */
        val savedTheme =
            getSharedPreferences(
                "wallet_settings",
                MODE_PRIVATE
            ).getString(
                "theme",
                "system"
            )

        when (savedTheme) {

            "light" ->
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )

            "dark" ->
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                    )

            else ->
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
        }

        preferences =
            getSharedPreferences(
                "wallet_settings",
                MODE_PRIVATE
            )

        tts =
            TextToSpeech(
                this,
                this
            )

        setContentView(
            R.layout.activity_main
        )

        setupDashboard()
    }

    override fun onResume() {
        super.onResume()

        if (::esewaOwner.isInitialized) {
            loadWalletInformation()
            updateNotificationAccess()
            loadRecentActivity()
        }
    }

    private fun setupDashboard() {

        esewaOwner =
            findViewById(
                R.id.esewaOwner
            )

        khaltiOwner =
            findViewById(
                R.id.khaltiOwner
            )

        esewaStatus =
            findViewById(
                R.id.esewaStatus
            )

        khaltiStatus =
            findViewById(
                R.id.khaltiStatus
            )

        recentActivityContainer =
            findViewById(
                R.id.recentActivityContainer
            )

        emptyActivityCard =
            findViewById(
                R.id.emptyActivityCard
            )

        /*
         * Settings button
         */
        findViewById<ImageButton>(
            R.id.settingsButton
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }

        /*
         * eSewa card opens Wallet settings.
         */
        findViewById<View>(
            R.id.esewaCard
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    WalletsActivity::class.java
                )
            )
        }

        /*
         * Khalti card opens Wallet settings.
         */
        findViewById<View>(
            R.id.khaltiCard
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    WalletsActivity::class.java
                )
            )
        }

        /*
         * View all activity.
         */
        findViewById<View>(
            R.id.viewActivity
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ActivityHistoryActivity::class.java
                )
            )
        }

        /*
         * Bottom navigation.
         */
        findViewById<View>(
            R.id.navHome
        ).setOnClickListener {
            // Already on Home.
        }

        findViewById<View>(
            R.id.navWallets
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    WalletsActivity::class.java
                )
            )
        }

        findViewById<View>(
            R.id.navActivity
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ActivityHistoryActivity::class.java
                )
            )
        }

        findViewById<View>(
            R.id.navSettings
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }

        loadWalletInformation()
        updateNotificationAccess()
        loadRecentActivity()
    }

    private fun loadWalletInformation() {

        val esewaName =
            preferences.getString(
                "esewa_owner_name",
                ""
            )?.trim()

        val khaltiName =
            preferences.getString(
                "khalti_owner_name",
                ""
            )?.trim()

        /*
         * Display eSewa owner.
         */
        esewaOwner.text =
            if (!esewaName.isNullOrBlank()) {
                esewaName
            } else {
                getString(
                    R.string.not_configured
                )
            }

        /*
         * Display Khalti owner.
         */
        khaltiOwner.text =
            if (!khaltiName.isNullOrBlank()) {
                khaltiName
            } else {
                getString(
                    R.string.not_configured
                )
            }

        /*
         * Determine whether eSewa announcements
         * are enabled.
         */
        val esewaEnabled =
            preferences.getBoolean(
                "esewa_received_enabled",
                true
            ) ||
                    preferences.getBoolean(
                        "esewa_sent_enabled",
                        true
                    )

        /*
         * Determine whether Khalti announcements
         * are enabled.
         */
        val khaltiEnabled =
            preferences.getBoolean(
                "khalti_received_enabled",
                true
            ) ||
                    preferences.getBoolean(
                        "khalti_sent_enabled",
                        true
                    )

        /*
         * eSewa status.
         */
        if (
            !esewaName.isNullOrBlank() &&
            esewaEnabled
        ) {

            esewaStatus.text =
                getString(
                    R.string.connected
                )

            esewaStatus.setTextColor(
                getColor(
                    R.color.success
                )
            )

        } else {

            esewaStatus.text =
                getString(
                    R.string.not_configured
                )

            esewaStatus.setTextColor(
                getColor(
                    R.color.text_secondary
                )
            )
        }

        /*
         * Khalti status.
         */
        if (
            !khaltiName.isNullOrBlank() &&
            khaltiEnabled
        ) {

            khaltiStatus.text =
                getString(
                    R.string.connected
                )

            khaltiStatus.setTextColor(
                getColor(
                    R.color.success
                )
            )

        } else {

            khaltiStatus.text =
                getString(
                    R.string.not_configured
                )

            khaltiStatus.setTextColor(
                getColor(
                    R.color.text_secondary
                )
            )
        }
    }

    private fun updateNotificationAccess() {

        val enabledListeners =
            Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )

        val enabled =
            enabledListeners
                ?.contains(packageName) == true

        val serviceStatus =
            findViewById<TextView>(
                R.id.serviceStatus
            )

        val serviceDescription =
            findViewById<TextView>(
                R.id.serviceDescription
            )

        if (enabled) {

            serviceStatus.text =
                getString(
                    R.string.active
                )

            serviceStatus.setTextColor(
                getColor(
                    R.color.success
                )
            )

            serviceDescription.text =
                getString(
                    R.string.ready_to_announce
                )

        } else {

            serviceStatus.text =
                getString(
                    R.string.disabled
                )

            serviceStatus.setTextColor(
                getColor(
                    R.color.danger
                )
            )

            serviceDescription.text =
                getString(
                    R.string.notification_access_disabled
                )
        }
    }

    private fun loadRecentActivity() {

        recentActivityContainer.removeAllViews()

        val transactions =
            TransactionHistory
                .getAll(this)
                .take(3)

        if (transactions.isEmpty()) {

            recentActivityContainer.visibility =
                View.GONE

            emptyActivityCard.visibility =
                View.VISIBLE

            return
        }

        recentActivityContainer.visibility =
            View.VISIBLE

        emptyActivityCard.visibility =
            View.GONE

        transactions.forEach { transaction ->

            recentActivityContainer.addView(
                createTransactionCard(
                    transaction
                )
            )
        }
    }

    private fun createTransactionCard(
        transaction: TransactionRecord
    ): View {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.background =
            getDrawable(
                R.drawable.bg_card
            )

        card.setPadding(
            18,
            14,
            18,
            14
        )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            0,
            0,
            10
        )

        card.layoutParams =
            params

        /*
         * Wallet + transaction type
         */
        val topRow =
            LinearLayout(this)

        topRow.orientation =
            LinearLayout.HORIZONTAL

        topRow.gravity =
            Gravity.CENTER_VERTICAL

        val walletText =
            TextView(this)

        walletText.text =
            transaction.wallet

        walletText.setTextColor(
            getColor(
                R.color.text_primary
            )
        )

        walletText.textSize =
            14f

        walletText.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val typeText =
            TextView(this)

        typeText.text =
            if (
                transaction.type == "received"
            ) {
                getString(
                    R.string.received
                )
            } else {
                getString(
                    R.string.sent
                )
            }

        typeText.setTextColor(
            if (
                transaction.type == "received"
            ) {
                getColor(
                    R.color.received
                )
            } else {
                getColor(
                    R.color.sent
                )
            }
        )

        typeText.textSize =
            12f

        val typeParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        typeParams.marginStart =
            8

        topRow.addView(
            walletText
        )

        topRow.addView(
            typeText,
            typeParams
        )

        card.addView(
            topRow
        )

        /*
         * Amount
         */
        val amountText =
            TextView(this)

        amountText.text =
            "NPR ${transaction.amount}"

        amountText.setTextColor(
            getColor(
                R.color.text_primary
            )
        )

        amountText.textSize =
            18f

        amountText.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val amountParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        amountParams.topMargin =
            6

        card.addView(
            amountText,
            amountParams
        )

        /*
         * Sender/person, when available.
         */
        if (
            !transaction.person.isNullOrBlank()
        ) {

            val personText =
                TextView(this)

            personText.text =
                transaction.person

            personText.setTextColor(
                getColor(
                    R.color.text_secondary
                )
            )

            personText.textSize =
                12f

            val personParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            personParams.topMargin =
                2

            card.addView(
                personText,
                personParams
            )
        }

        /*
         * Date/time.
         */
        val dateText =
            TextView(this)

        dateText.text =
            SimpleDateFormat(
                "MMM d, h:mm a",
                Locale.US
            ).format(
                Date(
                    transaction.timestamp
                )
            )

        dateText.setTextColor(
            getColor(
                R.color.text_muted
            )
        )

        dateText.textSize =
            11f

        val dateParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        dateParams.topMargin =
            4

        card.addView(
            dateText,
            dateParams
        )

        return card
    }

    override fun onInit(
        status: Int
    ) {

        if (
            status != TextToSpeech.SUCCESS
        ) {
            ttsReady = false
            return
        }

        val result =
            tts?.setLanguage(
                Locale.forLanguageTag(
                    "ne-NP"
                )
            )

        ttsReady =
            result !=
                    TextToSpeech.LANG_MISSING_DATA &&
                    result !=
                    TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }
}