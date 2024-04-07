package ru.devyandex.investmenthelper.service.core.util

import com.opencsv.CSVWriter
import org.junit.jupiter.api.Test
import org.ta4j.core.BaseBar
import ru.devyandex.investmenthelper.util.toBaseBar
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.contract.v1.RealExchange
import ru.tinkoff.piapi.contract.v1.ShareType
import ru.tinkoff.piapi.core.InvestApi
import java.io.FileWriter
import java.time.*
import java.time.format.DateTimeFormatter

class TinkoffMarketDataDownloader {
    private val token = ""
    private val api = InvestApi.createSandbox(token)

    fun getCandlesForPeriod(from: Instant, to: Instant, instrument: String, interval: CandleInterval) =
        api
            .marketDataService
            .getCandlesSync(instrument, from, to, interval)


    fun instrument() =
        api
            .instrumentsService
            .tradableSharesSync
            .filter {
                SHARE_TYPES.contains(it.shareType) &&
                        it.apiTradeAvailableFlag &&
                        !it.forQualInvestorFlag &&
                        it.buyAvailableFlag &&
                        it.sellAvailableFlag &&
                        it.realExchange == RealExchange.REAL_EXCHANGE_MOEX

            }

    @Test
    fun saveTradableShareData() {
        instrument()
            .forEach { share ->
                get5MinuteCandlesForLastNDays(share.figi, share.name, 30)
            }
    }

    fun get5MinuteCandlesForLastNDays(instrument: String, name: String, days: Long) {
        var beginDate = LocalDate.now().minusDays(days)
        val path = "C:\\progs\\local\\data\\"

        val outputFileName = "$path${name}_${instrument}_5_MIN_FROM_${beginDate}_TO_${LocalDate.now().plusDays(1)}.csv"

        val bars = mutableListOf<HistoricCandle>()

        println("Reading candles from:$beginDate to ${LocalDate.now()}")
        do {
            try {
                getCandlesForPeriod(
                    beginDate.atTime(LocalTime.MIN).toInstant(ZoneOffset.ofHours(3)),
                    beginDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.ofHours(3)),
                    instrument,
                    CandleInterval.CANDLE_INTERVAL_5_MIN
                ).also { bars.addAll(it) }

                beginDate = beginDate.plusDays(1)
                println("Reading data for: $beginDate")
            }catch (ex: Exception) {
                println("Too many requests, sleeping for 20sec. ")
                Thread.sleep(20000)
            }
        } while (beginDate < LocalDate.now().plusDays(1) && bars.isNotEmpty())

        if (bars.isNotEmpty()) {
            CSVWriter(FileWriter(outputFileName), ',', '\u0000', '\u0000', "\n").use { writer ->
                writer.writeNext(arrayOf(instrument, name, days.toString()))
                bars
                    .map { it.toBaseBar(Duration.ofMinutes(5)) }
                    .forEach { bar ->
                        writer.writeNext(bar.toStringArray())
                    }
            }
        }
    }

    private fun BaseBar.toStringArray() =
        arrayOf(
            this.endTime.format(DateTimeFormatter.ISO_INSTANT),
            this.openPrice.toString(),
            this.highPrice.toString(),
            this.lowPrice.toString(),
            this.closePrice.toString(),
            this.volume.toString()
        )

    companion object {
        private val SHARE_TYPES = listOf(ShareType.forNumber(1), ShareType.forNumber(2))
    }
}