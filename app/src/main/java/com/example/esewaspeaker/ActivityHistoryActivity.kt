package com.example.esewaspeaker

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityHistoryActivity : AppCompatActivity(){

    private lateinit var transactionContainer: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var transactionCount: TextView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_activity
        )

        transactionContainer =
            findViewById(
                R.id.transactionContainer
            )

        emptyState =
            findViewById(
                R.id.emptyState
            )

        transactionCount =
            findViewById(
                R.id.transactionCount
            )

        findViewById<View>(
            R.id.backButton
        ).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        loadTransactions()
    }

    private fun loadTransactions() {

        transactionContainer.removeAllViews()

        val transactions =
            TransactionHistory.getAll(this)

        transactionCount.text =
            transactions.size.toString()

        if (transactions.isEmpty()) {

            emptyState.visibility =
                View.VISIBLE

            findViewById<View>(
                R.id.activityScroll
            ).visibility =
                View.GONE

            return
        }

        emptyState.visibility =
            View.GONE

        findViewById<View>(
            R.id.activityScroll
        ).visibility =
            View.VISIBLE

        transactions.forEach { transaction ->

            transactionContainer.addView(
                createTransactionView(
                    transaction
                )
            )
        }
    }

    private fun createTransactionView(
        transaction: TransactionRecord
    ): View {

        val card =
            LinearLayout(this)

        card.orientation =
            LinearLayout.VERTICAL

        card.setBackgroundResource(
            R.drawable.bg_card
        )

        card.setPadding(
            dp(16),
            dp(14),
            dp(16),
            dp(14)
        )

        val cardParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        cardParams.bottomMargin =
            dp(10)

        card.layoutParams =
            cardParams

        val topRow =
            LinearLayout(this)

        topRow.orientation =
            LinearLayout.HORIZONTAL

        topRow.gravity =
            android.view.Gravity.CENTER_VERTICAL

        val wallet =
            TextView(this)

        wallet.text =
            transaction.wallet

        wallet.setTextColor(
            getColor(R.color.text_primary)
        )

        wallet.textSize =
            14f

        wallet.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        topRow.addView(
            wallet,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val type =
            TextView(this)

        val isReceived =
            transaction.type == "received"

        type.text =
            if (isReceived) {
                getString(R.string.received)
            } else {
                getString(R.string.sent)
            }

        type.setTextColor(
            getColor(
                if (isReceived) {
                    R.color.received
                } else {
                    R.color.sent
                }
            )
        )

        type.textSize =
            12f

        type.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        topRow.addView(type)

        card.addView(topRow)

        val amount =
            TextView(this)

        val formattedAmount =
            formatAmount(
                transaction.amount
            )

        amount.text =
            "NPR $formattedAmount"

        amount.setTextColor(
            getColor(
                if (isReceived) {
                    R.color.received
                } else {
                    R.color.sent
                }
            )
        )

        amount.textSize =
            18f

        amount.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val amountParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        amountParams.topMargin =
            dp(8)

        card.addView(
            amount,
            amountParams
        )

        transaction.person
            ?.takeIf { it.isNotBlank() }
            ?.let { personName ->

                val person =
                    TextView(this)

                person.text =
                    if (isReceived) {
                        "From $personName"
                    } else {
                        "To $personName"
                    }

                person.setTextColor(
                    getColor(
                        R.color.text_secondary
                    )
                )

                person.textSize =
                    12f

                val personParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                personParams.topMargin =
                    dp(3)

                card.addView(
                    person,
                    personParams
                )
            }

        val date =
            TextView(this)

        date.text =
            formatDate(
                transaction.timestamp
            )

        date.setTextColor(
            getColor(
                R.color.text_muted
            )
        )

        date.textSize =
            11f

        val dateParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        dateParams.topMargin =
            dp(6)

        card.addView(
            date,
            dateParams
        )

        return card
    }

    private fun formatAmount(
        amount: String
    ): String {
        return try {
            val number =
                amount.toDouble()

            if (number % 1.0 == 0.0) {
                String.format(
                    Locale.US,
                    "%,.0f",
                    number
                )
            } else {
                String.format(
                    Locale.US,
                    "%,.2f",
                    number
                )
            }
        } catch (e: Exception) {
            amount
        }
    }

    private fun formatDate(
        timestamp: Long
    ): String {

        val formatter =
            SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            )

        return formatter.format(
            Date(timestamp)
        )
    }

    private fun dp(
        value: Int
    ): Int {
        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }
}