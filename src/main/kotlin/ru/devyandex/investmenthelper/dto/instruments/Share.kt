package ru.devyandex.investmenthelper.dto.instruments

data class Share(
    val ticker: String,
    val lot: Int,
    val name: String,
    val shortEnabledFlag: Boolean
)