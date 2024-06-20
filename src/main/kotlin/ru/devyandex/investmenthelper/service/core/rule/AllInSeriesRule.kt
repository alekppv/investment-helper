package ru.devyandex.investmenthelper.service.core.rule

import org.ta4j.core.Rule
import org.ta4j.core.TradingRecord
import org.ta4j.core.rules.AbstractRule
import kotlin.math.max

class AllInSeriesRule(
    private val rule: Rule,
    private val barCount: Int
): AbstractRule() {
    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        for (i in max(0, (index - barCount + 1)) .. index) {
            if (!rule.isSatisfied(i)) return false
        }

        return true
    }
}