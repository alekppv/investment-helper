package ru.devyandex.investmenthelper.service.core.marketdata

import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.core.InvestApi
import ru.tinkoff.piapi.core.stream.StreamProcessor
import java.time.Instant
import java.util.function.Consumer

interface IMarketDataService {
    fun subscribeToCandlesStream(
        id: Long,
        instrument: String,
        streamProcessor: StreamProcessor<MarketDataResponse>,
        onErrorCallback: Consumer<Throwable>,
        interval: SubscriptionInterval
    ): Unit?

    fun unsubscribeFromCandleStream(
        id: Long,
        instrument: String
    ): Unit?

    fun getCandles(
        investApi: InvestApi,
        instrument: String,
        from: Instant,
        to: Instant,
        candleInterval: CandleInterval
    ): List<HistoricCandle>
}