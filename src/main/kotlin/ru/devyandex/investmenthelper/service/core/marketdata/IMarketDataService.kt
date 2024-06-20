package ru.devyandex.investmenthelper.service.core.marketdata

import org.ta4j.core.Bar
import ru.devyandex.investmenthelper.dto.enums.Interval
import java.time.Instant
import java.util.function.Consumer

interface IMarketDataService {
    fun subscribeToCandlesStream(
        id: Long,
        instrument: String,
        streamProcessor: (Bar) -> Unit,
        onErrorCallback: Consumer<Throwable>,
        interval: Interval
    ): Unit?

    fun unsubscribeFromCandleStream(
        id: Long,
        instrument: String
    ): Unit?

    fun getCandles(
        id: Long,
        instrument: String,
        from: Instant,
        to: Instant,
        interval: Interval
    ): List<Bar>
}