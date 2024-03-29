package ru.devyandex.investmenthelper.service.core.strategy

import org.springframework.stereotype.Service
import org.ta4j.core.BaseBar
import org.ta4j.core.BaseBarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.ATRIndicator
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandFacade
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.OverIndicatorRule
import org.ta4j.core.rules.StopGainRule
import org.ta4j.core.rules.StopLossRule
import org.ta4j.core.rules.UnderIndicatorRule
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.dto.strategy.StrategyEnum
import ru.devyandex.investmenthelper.dto.user.ApiClientDto
import ru.devyandex.investmenthelper.service.core.apiclient.InvestApiClientProvider
import ru.devyandex.investmenthelper.service.core.marketdata.IMarketDataService
import ru.devyandex.investmenthelper.service.core.rule.AllInSeriesRule
import ru.devyandex.investmenthelper.service.core.rule.OverOrEqualIndicatorRule
import ru.devyandex.investmenthelper.service.core.rule.UnderOrEqualIndicatorRule
import ru.devyandex.investmenthelper.util.toBaseBar
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class SimpleScalpingStrategy(
    private val clientProvider: InvestApiClientProvider,
    private val marketDataService: IMarketDataService
) : Strategy {
    override fun getName() = StrategyEnum.SIMPLE_SCALPING

    private val barSeriesStorage = ConcurrentHashMap<Long, BaseBarSeries>()

    override fun startProcessing(id: Long, companyStrategy: CompanyStrategy) {
        val client = clientProvider.getClient(id) ?: throw Exception()
        val duration = Duration.ofMinutes(5)

        importHistoricalData(client, id, companyStrategy, duration)
        //subscribeToNewData(id, companyStrategy, duration)

        createIndicators(id)
    }

    private fun createIndicators(id: Long) {
        barSeriesStorage[id]?.let { barSeries ->
            val closePriceIndicator = ClosePriceIndicator(barSeries)
            val emaFast = EMAIndicator(closePriceIndicator, EMA_FAST_BAR_COUNT)
            val emaSlow = EMAIndicator(closePriceIndicator, EMA_SLOW_BAR_COUNT)
            val bollingerBandFacade =
                BollingerBandFacade(closePriceIndicator, BOLLINGER_BAR_COUNT, BOLLINGER_MULTIPLIER)
            val atr = ATRIndicator(barSeries, ATR_BAR_COUNT)

            val longStrategy = BaseStrategy(
                "SIMPLE_SCALPING_LONG_0.0.1",
                emaLongEntryRule(emaFast, emaSlow, closePriceIndicator, bollingerBandFacade),
                StopLossRule(closePriceIndicator, BigDecimal(1.1)).and(
                    StopGainRule(
                        closePriceIndicator,
                        BigDecimal(1.5)
                    )
                )
            )

            val shortStrategy = BaseStrategy(
                "SIMPLE_SCALPING_SHORT_0.0.1",
                emaShortEntryRule(emaFast, emaSlow, closePriceIndicator, bollingerBandFacade),
                StopGainRule(closePriceIndicator, BigDecimal(1.1)).and(
                    StopLossRule(
                        closePriceIndicator,
                        BigDecimal(1.5)
                    )
                )
            )
        }
    }

    private fun emaLongEntryRule(
        emaFast: EMAIndicator,
        emaSlow: EMAIndicator,
        closePriceIndicator: ClosePriceIndicator,
        bollingerBandFacade: BollingerBandFacade
    ) =
        AllInSeriesRule(UnderIndicatorRule(emaFast, emaSlow), 6)
            .and(UnderOrEqualIndicatorRule(closePriceIndicator, bollingerBandFacade.lower()))

    private fun emaShortEntryRule(
        emaFast: EMAIndicator,
        emaSlow: EMAIndicator,
        closePriceIndicator: ClosePriceIndicator,
        bollingerBandFacade: BollingerBandFacade
    ) =
        AllInSeriesRule(OverIndicatorRule(emaFast, emaSlow), 6)
            .and(OverOrEqualIndicatorRule(closePriceIndicator, bollingerBandFacade.upper()))


    private fun subscribeToNewData(
        id: Long,
        companyStrategy: CompanyStrategy,
        duration: Duration
    ) {
        marketDataService.subscribeToCandlesStream(
            id,
            companyStrategy.id,
            { marketDataResponse ->
                appendBar(id, marketDataResponse.candle.toBaseBar(duration))
            },
            { error -> println(error.message) },
            SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES
        )
    }

    private fun importHistoricalData(
        client: ApiClientDto,
        id: Long,
        companyStrategy: CompanyStrategy,
        duration: Duration
    ) {
        marketDataService
            .getCandles(
                investApi = client.apiClient,
                from = OffsetDateTime.now().minusDays(5).toInstant(),
                to = Instant.now(),
                candleInterval = CandleInterval.CANDLE_INTERVAL_5_MIN,
                instrument = companyStrategy.id
            ).map { appendBar(id, it.toBaseBar(duration)) }
    }

    private fun appendBar(id: Long, bar: BaseBar) {
        barSeriesStorage[id]?.addBar(bar)
            ?: { barSeriesStorage[id] = BaseBarSeries().also { it.addBar(bar) } }
    }

    companion object {
        private val ATR_BAR_COUNT = 7
        private val BOLLINGER_BAR_COUNT = 15
        private val BOLLINGER_MULTIPLIER = BigDecimal(1.5)
        private val EMA_FAST_BAR_COUNT = 30
        private val EMA_SLOW_BAR_COUNT = 50
    }
}