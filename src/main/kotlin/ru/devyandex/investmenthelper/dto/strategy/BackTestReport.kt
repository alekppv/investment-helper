package ru.devyandex.investmenthelper.dto.strategy

import org.jfree.chart.JFreeChart
import org.ta4j.core.TradingRecord
import ru.devyandex.investmenthelper.dto.enums.SignalType

class BackTestReport(
    val chart: JFreeChart,
    val tradingRecords: Map<SignalType, TradingRecord>
)