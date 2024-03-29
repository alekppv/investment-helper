package ru.devyandex.investmenthelper.util

import org.ta4j.core.BaseBar
import ru.tinkoff.piapi.contract.v1.Candle
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.core.models.Money
import ru.tinkoff.piapi.core.utils.DateUtils.timestampToInstant
import ru.tinkoff.piapi.core.utils.MapperUtils.mapUnitsAndNanos
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

fun Money.toAmountCurrencyString() =
    "${this.value}${this.currency}"

fun HistoricCandle.toBaseBar(duration: Duration) =
    BaseBar(
        duration,
        ZonedDateTime.ofInstant(timestampToInstant(this.time), ZoneId.of("UTC")),
        mapUnitsAndNanos(this.open.units, this.open.nano),
        mapUnitsAndNanos(this.high.units, this.high.nano),
        mapUnitsAndNanos(this.low.units, this.low.nano),
        mapUnitsAndNanos(this.low.units, this.low.nano),
        this.volume.toBigDecimal()
    )

fun Candle.toBaseBar(duration: Duration) =
    BaseBar(
        duration,
        ZonedDateTime.ofInstant(timestampToInstant(this.time), ZoneId.of("UTC")),
        mapUnitsAndNanos(this.open.units, this.open.nano),
        mapUnitsAndNanos(this.high.units, this.high.nano),
        mapUnitsAndNanos(this.low.units, this.low.nano),
        mapUnitsAndNanos(this.low.units, this.low.nano),
        this.volume.toBigDecimal()
    )