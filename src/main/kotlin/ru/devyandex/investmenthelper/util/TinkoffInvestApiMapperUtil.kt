package ru.devyandex.investmenthelper.util

import org.ta4j.core.BaseBar
import ru.devyandex.investmenthelper.constants.Constants.CURRENCY
import ru.devyandex.investmenthelper.dto.enums.Interval
import ru.devyandex.investmenthelper.dto.enums.Interval.*
import ru.tinkoff.piapi.contract.v1.Candle
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.core.models.Money
import ru.tinkoff.piapi.core.utils.DateUtils.timestampToInstant
import ru.tinkoff.piapi.core.utils.MapperUtils.mapUnitsAndNanos
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

fun Money.toAmountCurrencyString() =
    "${this.value}${this.currency}"

fun String?.toNumberAmountWithoutCurrency() =
    this?.removeSuffix(CURRENCY)?.toBigDecimal()?.setScale(2)

fun HistoricCandle.toBaseBar(duration: Duration) =
    BaseBar(
        duration,
        ZonedDateTime.ofInstant(timestampToInstant(this.time), ZoneId.of("UTC")),
        mapUnitsAndNanos(this.open.units, this.open.nano),
        mapUnitsAndNanos(this.high.units, this.high.nano),
        mapUnitsAndNanos(this.low.units, this.low.nano),
        mapUnitsAndNanos(this.close.units, this.close.nano),
        this.volume.toBigDecimal()
    )

fun Candle.toBaseBar(duration: Duration) =
    BaseBar(
        duration,
        ZonedDateTime.ofInstant(timestampToInstant(this.time), ZoneId.of("UTC")),
        mapUnitsAndNanos(this.open.units, this.open.nano),
        mapUnitsAndNanos(this.high.units, this.high.nano),
        mapUnitsAndNanos(this.low.units, this.low.nano),
        mapUnitsAndNanos(this.close.units, this.close.nano),
        this.volume.toBigDecimal()
    )

fun Interval.toCandleInterval() =
    when (this) {
        INTERVAL_1_MIN-> CandleInterval.CANDLE_INTERVAL_1_MIN
        INTERVAL_5_MIN -> CandleInterval.CANDLE_INTERVAL_5_MIN
        INTERVAL_15_MIN -> CandleInterval.CANDLE_INTERVAL_15_MIN
        INTERVAL_HOUR -> CandleInterval.CANDLE_INTERVAL_HOUR
        INTERVAL_DAY-> CandleInterval.CANDLE_INTERVAL_DAY
        INTERVAL_2_MIN-> CandleInterval.CANDLE_INTERVAL_2_MIN
        INTERVAL_3_MIN-> CandleInterval.CANDLE_INTERVAL_3_MIN
        INTERVAL_10_MIN -> CandleInterval.CANDLE_INTERVAL_10_MIN
        INTERVAL_30_MIN -> CandleInterval.CANDLE_INTERVAL_30_MIN
        INTERVAL_2_HOUR -> CandleInterval.CANDLE_INTERVAL_2_HOUR
        INTERVAL_4_HOUR -> CandleInterval.CANDLE_INTERVAL_4_HOUR
        INTERVAL_WEEK -> CandleInterval.CANDLE_INTERVAL_WEEK
        INTERVAL_30_DAYS -> CandleInterval.CANDLE_INTERVAL_MONTH
    }

fun Interval.toSubscriptionInterval() =
    when (this) {
        INTERVAL_1_MIN-> SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
        INTERVAL_5_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES
        INTERVAL_15_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIFTEEN_MINUTES
        INTERVAL_HOUR -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_HOUR
        INTERVAL_DAY-> SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_DAY
        INTERVAL_2_MIN-> SubscriptionInterval.SUBSCRIPTION_INTERVAL_2_MIN
        INTERVAL_3_MIN-> SubscriptionInterval.SUBSCRIPTION_INTERVAL_3_MIN
        INTERVAL_10_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_10_MIN
        INTERVAL_30_MIN -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_30_MIN
        INTERVAL_2_HOUR -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_2_HOUR
        INTERVAL_4_HOUR -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_4_HOUR
        INTERVAL_WEEK -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_WEEK
        INTERVAL_30_DAYS -> SubscriptionInterval.SUBSCRIPTION_INTERVAL_MONTH
    }