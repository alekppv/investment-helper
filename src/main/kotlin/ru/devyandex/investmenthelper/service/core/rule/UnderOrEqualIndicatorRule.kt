package ru.devyandex.investmenthelper.service.core.rule

import org.ta4j.core.Indicator
import org.ta4j.core.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.rules.AbstractRule

class UnderOrEqualIndicatorRule(
    private val first: Indicator<Num>?,
    private val second: Indicator<Num>?
) : AbstractRule() {

    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        val satisfied = first!!.getValue(index).isLessThanOrEqual(second!!.getValue(index))
        traceIsSatisfied(index, satisfied)
        return satisfied
    }
}