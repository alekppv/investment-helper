package ru.devyandex.investmenthelper.service.core.marketdata

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service
import org.ta4j.core.Bar
import ru.devyandex.investmenthelper.dto.enums.Interval
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider
import ru.devyandex.investmenthelper.util.toBaseBar
import ru.devyandex.investmenthelper.util.toCandleInterval
import ru.devyandex.investmenthelper.util.toSubscriptionInterval
import java.time.Instant
import java.util.function.Consumer

@Service
@ConditionalOnMissingBean
class MarketDataServiceTinkoffImpl(
    private val clientProvider: InvestApiClientProvider
) : IMarketDataService {

    //TODO Добавить проверку на отсутствие клиента в хранилище
    override fun subscribeToCandlesStream(
        id: Long,
        instrument: String,
        streamProcessor: (Bar) -> Unit,
        onErrorCallback: Consumer<Throwable>,
        interval: Interval
    ) = clientProvider
        .getClient(id)
        ?.apiClient
        ?.marketDataStreamService
        ?.newStream(
            id.toString(),
            { response -> streamProcessor(response.candle.toBaseBar(interval.toDuration())) },
            onErrorCallback
        )?.subscribeCandles(listOf(instrument), interval.toSubscriptionInterval())


    override fun unsubscribeFromCandleStream(
        id: Long,
        instrument: String
    ) = clientProvider
        .getClient(id)
        ?.apiClient
        ?.marketDataStreamService
        ?.getStreamById(id.toString())
        ?.unsubscribeCandles(listOf(instrument))

    override fun getCandles(
        id: Long,
        instrument: String,
        from: Instant,
        to: Instant,
        interval: Interval
    ): List<Bar> = clientProvider
        .getClient(id)
        ?.apiClient
        ?.marketDataService
        ?.getCandlesSync(instrument, from, to, interval.toCandleInterval())
        ?.map { it.toBaseBar(interval.toDuration()) }
        ?: emptyList()
}