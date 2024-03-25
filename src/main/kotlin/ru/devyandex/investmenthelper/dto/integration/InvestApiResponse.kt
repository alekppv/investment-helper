package ru.devyandex.investmenthelper.dto.integration

data class InvestApiResponse<T>(
    val data: T,
    val isSuccessful: Boolean = true,
    val isError: Boolean = false,
    val message: String? = null
)
