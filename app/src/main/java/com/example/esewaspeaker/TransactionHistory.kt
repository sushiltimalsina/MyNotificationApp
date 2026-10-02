package com.example.esewaspeaker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class TransactionRecord(
    val wallet: String,
    val type: String,
    val amount: String,
    val person: String?,
    val timestamp: Long
)

object TransactionHistory {

    private const val PREFS = "wallet_history"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val MAX_TRANSACTIONS = 100

    private fun preferences(context: Context) =
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

    fun add(
        context: Context,
        transaction: TransactionRecord
    ) {
        val current = getAll(context).toMutableList()

        current.add(0, transaction)

        val limited =
            current.take(MAX_TRANSACTIONS)

        val jsonArray = JSONArray()

        limited.forEach { item ->
            val json = JSONObject()

            json.put("wallet", item.wallet)
            json.put("type", item.type)
            json.put("amount", item.amount)
            json.put("person", item.person ?: JSONObject.NULL)
            json.put("timestamp", item.timestamp)

            jsonArray.put(json)
        }

        preferences(context)
            .edit()
            .putString(
                KEY_TRANSACTIONS,
                jsonArray.toString()
            )
            .apply()
    }

    fun getAll(
        context: Context
    ): List<TransactionRecord> {

        val raw =
            preferences(context)
                .getString(
                    KEY_TRANSACTIONS,
                    null
                )
                ?: return emptyList()

        return try {
            val jsonArray = JSONArray(raw)
            val result = mutableListOf<TransactionRecord>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)

                val person =
                    if (
                        json.isNull("person")
                    ) {
                        null
                    } else {
                        json.optString("person")
                            .takeIf { it.isNotBlank() }
                    }

                result.add(
                    TransactionRecord(
                        wallet = json.optString("wallet"),
                        type = json.optString("type"),
                        amount = json.optString("amount"),
                        person = person,
                        timestamp = json.optLong("timestamp")
                    )
                )
            }

            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear(
        context: Context
    ) {
        preferences(context)
            .edit()
            .remove(KEY_TRANSACTIONS)
            .apply()
    }
}