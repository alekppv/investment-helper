package ru.devyandex.investmenthelper.service.core.strategy

import org.ta4j.core.TradingRecord
import ru.devyandex.investmenthelper.dto.enums.SignalType
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.dto.enums.StrategyEnum
import ru.devyandex.investmenthelper.dto.strategy.BackTestReport
import ru.devyandex.investmenthelper.dto.strategy.StrategySignal

interface Strategy {
    fun getName(): StrategyEnum

    fun startProcessing(id: Long, companyStrategy: CompanyStrategy)

    fun checkCurrentState(id: Long): List<StrategySignal>

    fun backTest(id: Long, lastCandles: Int): Map<SignalType, TradingRecord>

    fun backTestWithChart(id: Long, lastBars: Int): BackTestReport
}
