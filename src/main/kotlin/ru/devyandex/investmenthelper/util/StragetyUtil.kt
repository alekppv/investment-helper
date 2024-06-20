package ru.devyandex.investmenthelper.util

import org.ta4j.core.Indicator
import org.ta4j.core.Strategy
import org.ta4j.core.num.Num
import ru.devyandex.investmenthelper.dto.enums.SignalType
import ru.devyandex.investmenthelper.dto.strategy.NumIndicatorWithName
import ru.devyandex.investmenthelper.dto.strategy.StrategyWrapper

fun Strategy.wrap(signalType: SignalType) = StrategyWrapper(signalType, this)

fun Indicator<Num>.withName(name: String) = NumIndicatorWithName(this, name)