package com.example.weatherwise.helpers

object NumberConverter {
    private val westernToEasternMap = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
    )

    fun convertToArabicNumerals(input: String): String {
        return input.map { char ->
            westernToEasternMap[char] ?: char
        }.joinToString("")
    }
}