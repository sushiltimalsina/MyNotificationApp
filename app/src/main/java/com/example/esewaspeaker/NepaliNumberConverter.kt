package com.example.esewaspeaker

object NepaliNumberConverter {

    private val ones = arrayOf(
        "",
        "एक",
        "दुई",
        "तीन",
        "चार",
        "पाँच",
        "छ",
        "सात",
        "आठ",
        "नौ",
        "दश",
        "एघार",
        "बाह्र",
        "तेह्र",
        "चौध",
        "पन्ध्र",
        "सोह्र",
        "सत्र",
        "अठार",
        "उन्नाइस",
        "बीस",
        "एक्काइस",
        "बाइस",
        "तेइस",
        "चौबीस",
        "पच्चीस",
        "छब्बीस",
        "सत्ताइस",
        "अठ्ठाइस",
        "उनन्तीस",
        "तीस",
        "एकतीस",
        "बत्तीस",
        "तेत्तीस",
        "चौँतीस",
        "पैँतीस",
        "छत्तीस",
        "सैँतीस",
        "अठतीस",
        "उनन्चालीस",
        "चालीस",
        "एकचालीस",
        "बयालीस",
        "त्रिचालीस",
        "चवालीस",
        "पैँतालीस",
        "छयालीस",
        "सच्चालीस",
        "अठचालीस",
        "उनन्चास",
        "पचास",
        "एकाउन्न",
        "बाउन्न",
        "त्रिपन्न",
        "चौवन्न",
        "पचपन्न",
        "छपन्न",
        "सन्ताउन्न",
        "अन्ठाउन्न",
        "उनन्साठी",
        "साठी",
        "एकसट्ठी",
        "बयसट्ठी",
        "त्रिसट्ठी",
        "चौँसट्ठी",
        "पैँसट्ठी",
        "छयसट्ठी",
        "सत्सट्ठी",
        "अठसट्ठी",
        "उनन्सत्तरी",
        "सत्तरी",
        "एकहत्तर",
        "बहत्तर",
        "त्रिहत्तर",
        "चौहत्तर",
        "पचहत्तर",
        "छयहत्तर",
        "सतहत्तर",
        "अठहत्तर",
        "उनासी",
        "असी",
        "एकासी",
        "बयासी",
        "त्रियासी",
        "चौरासी",
        "पचासी",
        "छयासी",
        "सतासी",
        "अठासी",
        "उनान्नब्बे",
        "नब्बे",
        "एकान्नब्बे",
        "बयानब्बे",
        "त्रियान्नब्बे",
        "चौरान्नब्बे",
        "पन्चान्नब्बे",
        "छयान्नब्बे",
        "सन्तान्नब्बे",
        "अन्ठान्नब्बे",
        "उनान्सय"
    )

    fun convert(number: String): String {
        return try {
            val clean = number
                .replace(",", "")
                .trim()

            val value = clean.toDouble()

            if (value == value.toLong().toDouble()) {
                convertInteger(value.toLong())
            } else {
                convertDecimal(clean)
            }
        } catch (e: Exception) {
            number
        }
    }

    private fun convertInteger(number: Long): String {

        if (number == 0L) return "शून्य"

        if (number < 100) {
            return ones[number.toInt()]
        }

        val parts = mutableListOf<String>()
        var remaining = number

        if (remaining >= 10000000) {
            val crore = remaining / 10000000
            parts.add(convertInteger(crore))
            parts.add("करोड")
            remaining %= 10000000
        }

        if (remaining >= 100000) {
            val lakh = remaining / 100000
            parts.add(convertInteger(lakh))
            parts.add("लाख")
            remaining %= 100000
        }

        if (remaining >= 1000) {
            val thousand = remaining / 1000
            parts.add(convertInteger(thousand))
            parts.add("हजार")
            remaining %= 1000
        }

        if (remaining >= 100) {
            val hundred = remaining / 100
            parts.add(convertInteger(hundred))
            parts.add("सय")
            remaining %= 100
        }

        if (remaining > 0) {
            parts.add(convertInteger(remaining))
        }

        return parts.joinToString(" ")
    }

    private fun convertDecimal(number: String): String {

        val parts = number.split(".", limit = 2)

        val integerPart = parts[0].toLongOrNull() ?: return number

        if (parts.size == 1) {
            return convertInteger(integerPart)
        }

        val decimalPart = parts[1]

        val integerWords = convertInteger(integerPart)

        if (decimalPart.all { it == '0' }) {
            return integerWords
        }

        val decimalWords = decimalPart
            .map { digit ->
                convertInteger(digit.digitToInt().toLong())
            }
            .joinToString(" ")

        return "$integerWords दशमलव $decimalWords"
    }
}