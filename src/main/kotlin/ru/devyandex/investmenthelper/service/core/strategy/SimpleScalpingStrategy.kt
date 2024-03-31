package ru.devyandex.investmenthelper.service.core.strategy

import org.springframework.stereotype.Service
import org.ta4j.core.*
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandFacade
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.OverIndicatorRule
import org.ta4j.core.rules.StopGainRule
import org.ta4j.core.rules.StopLossRule
import org.ta4j.core.rules.UnderIndicatorRule
import ru.devyandex.investmenthelper.dto.enums.Interval
import ru.devyandex.investmenthelper.dto.enums.SignalType
import ru.devyandex.investmenthelper.dto.enums.StrategyEnum
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.service.core.marketdata.IMarketDataService
import ru.devyandex.investmenthelper.service.core.rule.AllInSeriesRule
import ru.devyandex.investmenthelper.service.core.rule.OverOrEqualIndicatorRule
import ru.devyandex.investmenthelper.service.core.rule.UnderOrEqualIndicatorRule
import ru.devyandex.investmenthelper.util.withName
import ru.devyandex.investmenthelper.util.wrap
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime


@Service
class SimpleScalpingStrategy(
    private val marketDataService: IMarketDataService
) : AbstractStrategy() {
    override fun getName(): StrategyEnum = StrategyEnum.SIMPLE_SCALPING

    override fun startProcessing(id: Long, companyStrategy: CompanyStrategy) {
        val interval = Interval.INTERVAL_5_MIN

        importHistoricalData(id, companyStrategy, interval)
        subscribeToNewData(id, companyStrategy, interval)
        createIndicators(id)
    }

    private fun createIndicators(id: Long) {
        barSeriesStorage[id]?.let { barSeries ->
            val closePriceIndicator = ClosePriceIndicator(barSeries)
            val emaFast = EMAIndicator(closePriceIndicator, EMA_FAST_BAR_COUNT)
            val emaSlow = EMAIndicator(closePriceIndicator, EMA_SLOW_BAR_COUNT)
            val bollingerBandFacade =
                BollingerBandFacade(closePriceIndicator, BOLLINGER_BAR_COUNT, BOLLINGER_MULTIPLIER)

            addIndicators(
                id,
                emaSlow.withName("EmaSlow"),
                emaFast.withName("EmaFast"),
                bollingerBandFacade.lower().withName("BollingerLower"),
                bollingerBandFacade.upper().withName("BollingerUpper")
            )

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

            addStrategy(id, longStrategy.or(shortStrategy).wrap(SignalType.DEFAULT))
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
        interval: Interval
    ) = marketDataService.subscribeToCandlesStream(
            id,
            companyStrategy.id,
            { marketDataResponse ->
                appendBar(id, marketDataResponse)
            },
            { error -> println(error.message) },
            interval
        )

    private fun importHistoricalData(
        id: Long,
        companyStrategy: CompanyStrategy,
        interval: Interval
    ) = marketDataService
            .getCandles(
                id = id,
                from = OffsetDateTime.now().minusDays(5).toInstant(),
                to = Instant.now(),
                interval = interval,
                instrument = companyStrategy.id
            ).map {
                appendBar(id, it)
            }

    companion object {
        private const val BOLLINGER_BAR_COUNT = 15
        private val BOLLINGER_MULTIPLIER = BigDecimal(1.5)
        private const val EMA_FAST_BAR_COUNT = 30
        private const val EMA_SLOW_BAR_COUNT = 50
    }
}