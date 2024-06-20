package ru.devyandex.investmenthelper.service.core.marketdata

import com.opencsv.CSVReader
import org.springframework.util.ResourceUtils
import org.ta4j.core.Bar
import org.ta4j.core.BaseBar
import ru.devyandex.investmenthelper.dto.enums.Interval
import java.io.FileReader
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.function.Consumer

/**
 * Тестовая реализация IMarketDataService, предназначенная для проверки стратегий на тестовых данных
 * @property fileName - путь к CSV файлу, содержащему тестовые данных.
 * Пример формата данных:
 * Gmt time,Open,High,Low,Close,Volume
 * 2024-03-07T06:55:00Z,1.09425,1.09426,1.09405,1.09406,585.1
 */
class MarketDataFileMockService(fileName: String) : IMarketDataService {
    private val barList = mutableListOf<Bar>()

    companion object {
        private const val LINES_AMOUNT = 30000
    }

    init {
        try {
            CSVReader(
                FileReader(ResourceUtils.getFile(fileName))
            ).use { reader ->
                reader.readNext()

                val bars = mutableListOf<Bar>()

                for (lineCounter in 0..LINES_AMOUNT) {
                    reader
                        .readNext()
                        ?.let { line ->
                            try {
                                bars.add(
                                    BaseBar(
                                        Duration.ofMinutes(5),
                                        Instant.parse(line[0]).atZone(ZoneId.of("UTC"))
                                            .withZoneSameInstant(ZoneId.of("Europe/Moscow")),
                                        BigDecimal(line[1]),
                                        BigDecimal(line[2]),
                                        BigDecimal(line[3]),
                                        BigDecimal(line[4]),
                                        BigDecimal(line[5])
                                    )
                                )
                            } catch (ex: Exception) {
                                println(ex.message)
                            }
                        }
                }
                barList.addAll(bars)
            }
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    override fun subscribeToCandlesStream(
        id: Long,
        instrument: String,
        streamProcessor: (Bar) -> Unit,
        onErrorCallback: Consumer<Throwable>,
        interval: Interval
    ) {
    }

    override fun unsubscribeFromCandleStream(id: Long, instrument: String) {}

    override fun getCandles(
        id: Long,
        instrument: String,
        from: Instant,
        to: Instant,
        interval: Interval
    ): List<Bar> = barList
}