package ru.devyandex.investmenthelper.util

fun <T> wrapMethod(errorMessage: String, block: () -> T?) =
    try {
        block().toInvestApiResponse()
    } catch (ex: Exception) {
        createErrorResponse(message = errorMessage, isError = true)
    }