package ru.devyandex.investmenthelper.util

import ru.tinkoff.piapi.core.models.Money

fun Money.toAmountCurrencyString() =
    "${this.value}${this.currency}"