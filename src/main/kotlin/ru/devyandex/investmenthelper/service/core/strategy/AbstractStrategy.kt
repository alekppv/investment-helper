package ru.devyandex.investmenthelper.service.core.strategy

import org.apache.commons.lang3.math.NumberUtils.max
import org.jfree.chart.ChartFactory
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.Marker
import org.jfree.chart.plot.ValueMarker
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.CandlestickRenderer
import org.jfree.chart.renderer.xy.CandlestickRenderer.WIDTHMETHOD_SMALLEST
import org.jfree.chart.renderer.xy.XYItemRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.Minute
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.DefaultOHLCDataset
import org.jfree.data.xy.OHLCDataItem
import org.ta4j.core.*
import org.ta4j.core.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.BarSeriesManager
import org.ta4j.core.num.DecimalNum
import ru.devyandex.investmenthelper.dto.enums.SignalType
import ru.devyandex.investmenthelper.dto.enums.StrategyEnum
import ru.devyandex.investmenthelper.dto.enums.TradeType
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.dto.strategy.BackTestReport
import ru.devyandex.investmenthelper.dto.strategy.NumIndicatorWithName
import ru.devyandex.investmenthelper.dto.strategy.StrategySignal
import ru.devyandex.investmenthelper.dto.strategy.StrategyWrapper
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
     * @param lastCandles - Количество свечей с конца, по которым требуется провести расчет
     * @return TradingRecord, содержащий в себе историю сессии
     */
    override fun backTest(id: Long, lastCandles: Int): Map<SignalType, TradingRecord> =
        barSeriesStorage[id]?.let { bars ->
            val barSeriesManager =
                BarSeriesManager(bars.getSubSeries(max(bars.endIndex - lastCandles, bars.beginIndex), bars.endIndex))
            strategies[id]?.map { wrapper ->
                wrapper.signalType to barSeriesManager.run(wrapper.strategy)
            }
        }?.toMap() ?: emptyMap()

    /**
     * Метод тестирования стратегии ручным перебором свечей с проверкой shouldOperate.
     * Является временным решением для сравнения с библиотечным BarSeriesManager
     */
    private fun backTestIntoOneRecord(id: Long, lastCandles: Int): Map<SignalType, TradingRecord> =
        barSeriesStorage[id]?.let { bars ->
            val tradingRecord = BaseTradingRecord(
                Trade.TradeType.BUY,
                max(bars.endIndex - lastCandles, bars.beginIndex),
                bars.endIndex,
                ZeroCostModel(),
                ZeroCostModel()
            )
            bars
                .barData
                .mapIndexed { index, bar ->
                    strategies[id]?.map { wrapper ->
                        val shouldOperate = wrapper
                            .strategy
                            .shouldOperate(index, tradingRecord)

                        if (shouldOperate) {
                            tradingRecord.operate(index, bar.closePrice, DecimalNum.valueOf(3))
                        }
                    }
                }

            mapOf(SignalType.DEFAULT to tradingRecord)
        } ?: emptyMap()

    private fun longShortBackTest(id: Long, lastCandles: Int): Map<SignalType, TradingRecord> =
        barSeriesStorage[id]?.let { bars ->
            strategies[id]?.associate { wrapper ->
                val tradingRecord = BaseTradingRecord(
                    Trade.TradeType.BUY,
                    max(bars.endIndex - lastCandles, bars.beginIndex),
                    bars.endIndex,
                    ZeroCostModel(),
                    ZeroCostModel()
                )

                bars
                    .barData
                    .mapIndexed { index, bar ->
                        val shouldOperate = wrapper
                            .strategy
                            .shouldOperate(index, tradingRecord)

                        if (shouldOperate) {
                            tradingRecord.operate(index, bar.closePrice, DecimalNum.valueOf(3))
                        }
                    }
                wrapper.signalType to tradingRecord
            }
        } ?: emptyMap()


    /**
     * Метод подготовки графиков работы индикаторов.
     * @param id - Идентификатор пользователя.
     * @param lastBars - Количество свечей с конца, по которым будут готовиться графики.
     *
     * @return Экземпляр JFreeChart, содержащий в себе свечи и графики работы идентификаторов.
     */
//TODO Можно оптимизировать, избавившись от нескольких прогонов по списку баров
    override fun backTestWithChart(id: Long, lastBars: Int): BackTestReport {
        val data = TimeSeriesCollection()
        indicators[id]
            ?.forEach { numIndicatorWithNames ->
                val chartTimeSeries = TimeSeries(numIndicatorWithNames.name)
                val barSeries = numIndicatorWithNames.indicator.barSeries

                for (i in max(barSeries.endIndex - lastBars, barSeries.beginIndex)..barSeries.endIndex) {
                    chartTimeSeries.add(
                        Minute(Date.from(barSeries.getBar(i).endTime.toInstant())),
                        numIndicatorWithNames.indicator.getValue(i).doubleValue()
                    )
                }

                data.addSeries(chartTimeSeries)
            }

        val barSeries = barSeriesStorage[id] ?: BaseBarSeries()
        val subseries = barSeries
            .getSubSeries(max((barSeries.endIndex - lastBars), barSeries.beginIndex), barSeries.endIndex - 1)

        val ohlcData = subseries
            .barData
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

        chart.antiAlias = true

        val plot = chart.plot as XYPlot
        (plot.renderer as CandlestickRenderer).autoWidthMethod = WIDTHMETHOD_SMALLEST
        (plot.rangeAxis as NumberAxis).autoRangeIncludesZero = false
        val domainAxis = plot.domainAxis as DateAxis

        domainAxis.dateFormatOverride = SimpleDateFormat("yyyy-MM-dd HH:mm")

        val index = 1
        plot.setDataset(index, data)
        plot.mapDatasetToRangeAxis(index, 0)

        val renderer2: XYItemRenderer = XYLineAndShapeRenderer(true, false)
        plot.setRenderer(1, renderer2)
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD)

        val tradingRecords = backTest(id, lastBars)
            .onEach { (signal, tradingRecord) ->
                addBuySellSignals(
                    signal,
                    tradingRecord,
                    plot,
                    subseries
                )
            }

        return BackTestReport(chart, tradingRecords)
    }

    /**
     * Метод отрисовки сигналов на графике.
     * @param tradingRecord - История торговой сессии библиотеки ta4j
     * @param plot - класс для отрисовки элементов на графике
     * @param series - Список свечей
     */
//TODO Заставить работать. TradingRecord можно заменить на собственную сущность,
// чтобы можно было проверять на своих методах расчета
    private fun addBuySellSignals(signal: SignalType, tradingRecord: TradingRecord, plot: XYPlot, series: BarSeries) {
        tradingRecord
            .positions
            .forEach { position ->
                addPosition(signal, position, plot, series)
            }

        if (!tradingRecord.isClosed) {
            addPosition(signal, tradingRecord.currentPosition, plot, series)
        }
    }

    private fun addPosition(signal: SignalType, position: Position, plot: XYPlot, series: BarSeries) {
        val entryTime = if (position.entry.index > series.endIndex) {
            series.getBar(position.entry.index - 1).endTime.plusMinutes(5).toInstant()
        } else {
            series.getBar(position.entry.index).endTime.toInstant()
        }

        val buySignalBarTime = Minute(
            Date.from(entryTime)
        ).firstMillisecond.toDouble()

        val buyMarker: Marker = ValueMarker(buySignalBarTime)
        buyMarker.paint = Color.GREEN
        buyMarker.label = signal.name.substring(0, 1)

        plot.addDomainMarker(buyMarker)

        position.exit?.let {
            val exitTime = if (position.exit.index > series.endIndex) {
                series.getBar(position.exit.index - 1).endTime.plusMinutes(5).toInstant()
            } else {
                series.getBar(position.exit.index).endTime.toInstant()
            }

            val sellSignalBarTime = Minute(
                Date.from(exitTime)
            ).firstMillisecond.toDouble()
            val sellMarker: Marker = ValueMarker(sellSignalBarTime)
            sellMarker.paint = Color.RED
            sellMarker.label = signal.name.substring(0, 1)

            plot.addDomainMarker(sellMarker)
        }
    }

    /**
     * Метод для добавления свеч во внутреннее хранилище
     * @param id - Идентификатор пользователя
     * @param bar - Свеча
     */
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

    /**
     * Метод для добавления стратегий во внутреннее хранилище
     * @param id - Идентификатор пользователя
     * @param strategyVararg - Стратегии
     */
    protected fun addStrategy(id: Long, vararg strategyVararg: StrategyWrapper) {
        val strategyList = strategies[id]
        if (strategyList != null) {
            strategyList.addAll(strategyVararg)
        } else {
            val newStorage = CopyOnWriteArrayList<StrategyWrapper>()
            newStorage.addAll(strategyVararg)
            strategies[id] = newStorage
        }
    }

    /**
     * Метод для добавления индикаторов во внутреннее хранилище
     * @param id - Идентификатор пользователя
     * @param indicatorVararg - Индикаторы
     */
    protected fun addIndicators(id: Long, vararg indicatorVararg: NumIndicatorWithName) {
        val indicatorList = indicators[id]
        if (indicatorList != null) {
            indicatorList.addAll(indicatorVararg)
        } else {
            val newStorage = CopyOnWriteArrayList<NumIndicatorWithName>()
            newStorage.addAll(indicatorVararg)
            indicators[id] = newStorage
        }
    }
}