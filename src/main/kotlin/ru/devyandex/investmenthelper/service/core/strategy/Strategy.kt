package ru.devyandex.investmenthelper.service.core.strategy

import org.jfree.chart.JFreeChart
import org.ta4j.core.TradingRecord
import ru.devyandex.investmenthelper.dto.enums.SignalType
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.dto.enums.StrategyEnum
import ru.devyandex.investmenthelper.dto.strategy.StrategySignal

interface Strategy {
    fun getName(): StrategyEnum

    fun startProcessing(id: Long, companyStrategy: CompanyStrategy)

    fun checkCurrentState(id: Long): List<StrategySignal>

    fun backTest(id: Long): Map<SignalType, TradingRecord>

    fun prepareChart(id: Long, lastBars: Int): JFreeChart
}
