package ru.devyandex.investmenthelper.dto.user

data class AccountDto(
    val id: String,
    val type: String,
    val name: String,
    val status: String
)