package ru.devyandex.investmenthelper.dto.strategy

import ru.devyandex.investmenthelper.dto.enums.SignalType
import ru.devyandex.investmenthelper.dto.enums.TradeType

data class StrategySignal(
    val signalType: SignalType,
    val tradeType: TradeType,
    val result: Boolean,
    val strategyName: String
)