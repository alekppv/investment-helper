package ru.devyandex.investmenthelper.util

import ru.devyandex.investmenthelper.dto.integration.InvestApiResponse

fun <T> T.toInvestApiResponse() = InvestApiResponse(data = this)

fun <T> createErrorResponse(message: String, isError: Boolean = false) = InvestApiResponse<T?>(
    data = null,
    isSuccessful = false,
    isError = isError,
    message = message
)