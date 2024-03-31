package ru.devyandex.investmenthelper.service.core.strategy

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.Marker
import org.jfree.chart.plot.ValueMarker
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.Minute
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.DefaultOHLCDataset
import org.jfree.data.xy.OHLCDataItem
import org.ta4j.core.Bar
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeries
import org.ta4j.core.TradingRecord
import org.ta4j.core.backtest.BarSeriesManager
import ru.devyandex.investmenthelper.dto.enums.SignalType
import ru.devyandex.investmenthelper.dto.enums.StrategyEnum
import ru.devyandex.investmenthelper.dto.enums.TradeType
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.dto.strategy.NumIndicatorWithName
import ru.devyandex.investmenthelper.dto.strategy.StrategySignal
import ru.devyandex.investmenthelper.dto.strategy.StrategyWrapper
import java.awt.BasicStroke
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Абстрактный класс для создания стратегий.
 * @property barSeriesStorage - хранилище свечей. Ключом является идентификатор пользователя.
 * @property strategies - Хранилище настроенных стратегий (сущностей библиотеки ta4j).
 * Ключом является идентификатор пользователя.
 * @property indicators - Хранилище индикаторов для настройки стратегий. Нужны для проверки текущего состояния
 * и подготовки отчетов/графиков. Ключом является идентификатор пользователя.
 */
abstract class AbstractStrategy : Strategy {
    protected val barSeriesStorage = ConcurrentHashMap<Long, BaseBarSeries>()
    protected val strategies = ConcurrentHashMap<Long, CopyOnWriteArrayList<StrategyWrapper>>()
    protected val indicators = ConcurrentHashMap<Long, CopyOnWriteArrayList<NumIndicatorWithName>>()

    /**
     * Имя стратегии.
     * @return одно из значений StrategyEnum.
     */
    abstract override fun getName(): StrategyEnum

    /**
     * Метод инициализации расчета по компании для пользователя.
     * @param id - Идентификатор пользователя.
     * @param companyStrategy - Настройки стратегии для компании.
     */
    abstract override fun startProcessing(id: Long, companyStrategy: CompanyStrategy)

    /**
     * Метод проверки текущих показателей стратегии для последней свечи.
     * @param id - Идентификатор пользователя
     * @return список рекомендаций для покупки/продажи
     */
    override fun checkCurrentState(id: Long): List<StrategySignal> =
        barSeriesStorage[id]?.let { baseBarSeries ->
            val lastBarIndex = baseBarSeries.endIndex
            strategies[id]?.let { strats ->
                strats.flatMap { wrapper ->
                    listOf(
                        StrategySignal(
                            signalType = wrapper.signalType,
                            tradeType = TradeType.ENTER,
                            result = wrapper.strategy.shouldEnter(lastBarIndex),
                            strategyName = wrapper.strategy.name
                        ),
                        StrategySignal(
                            signalType = wrapper.signalType,
                            tradeType = TradeType.EXIT,
                            result = wrapper.strategy.shouldExit(lastBarIndex),
                            strategyName = wrapper.strategy.name
                        )
                    )
                }
            }
        } ?: emptyList()

    /**
     * Метод для тестирования стратегии на данных из barSeriesStorage. Основное предназначение -
     * тестирование стратегии на заготовленных заранее данных.
     *
     * @param id - Идентификатор пользователя
     * @return TradingRecord, содержащий в себе историю сессии
     */
    override fun backTest(id: Long): Map<SignalType, TradingRecord> =
        barSeriesStorage[id]?.let { bars ->
            val barSeriesManager = BarSeriesManager(bars)
            strategies[id]?.map { wrapper ->
                wrapper.signalType to barSeriesManager.run(wrapper.strategy)
            }
        }?.toMap() ?: emptyMap()


    /**
     * Метод подготовки графиков работы индикаторов.
     * @param id - Идентификатор пользователя.
     * @param lastBars - Количество свечей с конца, по которым будут готовиться графики.
     *
     * @return Экземпляр JFreeChart, содержащий в себе свечи и графики работы идентификаторов.
     */
    //TODO Можно оптимизировать, избавившись от нескольких прогонов по списку баров
    override fun prepareChart(id: Long, lastBars: Int): JFreeChart {
        val data = TimeSeriesCollection()
        indicators[id]
            ?.filter { it.name != "ClosePrice" }
            ?.forEach { numIndicatorWithNames ->
                val chartTimeSeries = TimeSeries(numIndicatorWithNames.name)
                val barSeries = numIndicatorWithNames.indicator.barSeries

                for (i in barSeries.endIndex - lastBars..barSeries.endIndex) {
                    chartTimeSeries.add(
                        Minute(Date.from(barSeries.getBar(i).endTime.toInstant())),
                        numIndicatorWithNames.indicator.getValue(i).doubleValue()
                    )
                }

                data.addSeries(chartTimeSeries)
            }

        val barSeries = barSeriesStorage[id] ?: BaseBarSeries()

        val ohlcData = barSeries
            .barData
            .subList(barSeries.endIndex - lastBars, barSeries.endIndex - 1)
            .map {
                OHLCDataItem(
                    Date.from(it.endTime.toInstant()),
                    it.openPrice.doubleValue(),
                    it.highPrice.doubleValue(),
                    it.lowPrice.doubleValue(),
                    it.closePrice.doubleValue(),
                    it.volume.doubleValue()
                )
            }.toTypedArray()

        val ohlcDataset = DefaultOHLCDataset(
            "SimpleScalping${id}",
            ohlcData
        )

        val chart = ChartFactory.createCandlestickChart(
            getName().name,
            "Date",
            "IndicatorValue",
            ohlcDataset,
            true
        )

        val plot = chart.plot as XYPlot
        (plot.rangeAxis as NumberAxis).autoRangeIncludesZero = false
        val domainAxis = plot.domainAxis as DateAxis

        domainAxis.dateFormatOverride = SimpleDateFormat("yyyy-MM-dd HH:mm")

        val index = 1
        plot.setDataset(index, data)
        plot.mapDatasetToRangeAxis(index, 0)

        val renderer2: XYItemRenderer = XYLineAndShapeRenderer(true, false)
        plot.setRenderer(1, renderer2)
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD)

        backTest(id)
            .values
            .forEach { tradingRecord ->
                addBuySellSignals(
                    tradingRecord,
                    plot,
                    barSeries
                )
            }

        return chart
    }

    //TODO Заставить работать
    private fun addBuySellSignals(tradingRecord: TradingRecord, plot: XYPlot, series: BarSeries) =
        tradingRecord
            .positions
            .forEach { position ->
                val buySignalBarTime = Minute(
                    Date.from(series.getBar(position.entry.index).endTime.toInstant())
                ).firstMillisecond.toDouble()

                val buyMarker: Marker = ValueMarker(buySignalBarTime)
                buyMarker.paint = Color.GREEN
                buyMarker.label = "B"
                buyMarker.stroke = BasicStroke(100.0F)

                plot.addDomainMarker(buyMarker)

                val sellSignalBarTime = Minute(
                    Date.from(series.getBar(position.exit.index).endTime.toInstant())
                ).firstMillisecond.toDouble()
                val sellMarker: Marker = ValueMarker(sellSignalBarTime)
                sellMarker.paint = Color.RED
                sellMarker.label = "S"
                buyMarker.stroke = BasicStroke(100.0F)

                plot.addDomainMarker(sellMarker)
            }

    protected fun appendBar(id: Long, bar: Bar) {
        val storage = barSeriesStorage[id]
        if (storage != null) {
            try {
                storage.addBar(bar)
            } catch (ex: Exception) {
                println(ex.message)
            }
        } else {
            val newStorage = BaseBarSeries()
            newStorage.addBar(bar)
            barSeriesStorage[id] = newStorage
        }
    }

    protected fun addStrategy(id: Long, vararg strategy: StrategyWrapper) {
        val strategyList = strategies[id]
        if (strategyList != null) {
            strategyList.addAll(strategy)
        } else {
            val newStorage = CopyOnWriteArrayList<StrategyWrapper>()
            newStorage.addAll(strategy)
            strategies[id] = newStorage
        }
    }

    protected fun addIndicators(id: Long, vararg indicator: NumIndicatorWithName) {
        val indicatorList = indicators[id]
        if (indicatorList != null) {
            indicatorList.addAll(indicator)
        } else {
            val newStorage = CopyOnWriteArrayList<NumIndicatorWithName>()
            newStorage.addAll(indicator)
            indicators[id] = newStorage
        }
    }
}