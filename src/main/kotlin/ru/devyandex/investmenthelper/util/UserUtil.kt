package ru.devyandex.investmenthelper.util

import ru.devyandex.investmenthelper.dto.user.ApiClientDto

fun <T> ApiClientDto?.validateWithAccount() =
    if (this == null) {
        createErrorResponse<T>("Пользователь не найден")
    } else {
        if (this.accountId == null) {
            createErrorResponse<T>("Для выполнения операции необходимо выбрать счет")
        } else {
            this.toInvestApiResponse()
        }
    }