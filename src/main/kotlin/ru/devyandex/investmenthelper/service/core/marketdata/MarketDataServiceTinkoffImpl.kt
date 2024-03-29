package ru.devyandex.investmenthelper.service.core.marketdata

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.core.InvestApi
import ru.tinkoff.piapi.core.stream.StreamProcessor
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
        streamProcessor: StreamProcessor<MarketDataResponse>,
        onErrorCallback: Consumer<Throwable>,
        interval: SubscriptionInterval
    ) = clientProvider
        .getClient(id)
        ?.apiClient
        ?.marketDataStreamService
        ?.newStream(
            id.toString(),
            streamProcessor,
            onErrorCallback
        )?.subscribeCandles(listOf(instrument), interval)


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
        investApi: InvestApi,
        instrument: String,
        from: Instant,
        to: Instant,
        candleInterval: CandleInterval
    ): List<HistoricCandle> = investApi
        .marketDataService
        .getCandlesSync(
            instrument,
            from,
            to,
            candleInterval
        )
}