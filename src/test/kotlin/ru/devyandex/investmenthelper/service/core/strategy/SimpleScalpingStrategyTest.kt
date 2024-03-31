package ru.devyandex.investmenthelper.service.core.strategy

import org.jfree.chart.ChartUtils
import org.junit.jupiter.api.Test
import ru.devyandex.investmenthelper.dto.enums.StrategyEnum
import ru.devyandex.investmenthelper.dto.setting.CompanyStrategy
import ru.devyandex.investmenthelper.service.core.marketdata.MarketDataFileMockService
import java.io.File
import java.io.FileOutputStream
import java.util.*


class SimpleScalpingStrategyTest {

    /**
     * Тест предназначен для выгрузки заранее подготовленных данных и отрисовки по ним графиков индикаторов.
     * Сохраняет в outputFileName график индикаторов
     */
    @Test
    fun simpleScalpingStrategyBackTest() {
        //Путь cvs файла с тестовыми данными
        val inputFileName = "classpath:data/EURUSD_Candlestick_5_M_ASK_30.09.2019-30.09.2022.csv"
        //Путь выходного файла
        val outputFileName = "C:\\progs\\local\\chart${UUID.randomUUID()}.png"
        //Количество свечей с конца, по которым будет создан график
        val reportSize = 100

        val marketData = MarketDataFileMockService(inputFileName)

        val simpleScalpingStrategy = SimpleScalpingStrategy(marketData)
        simpleScalpingStrategy.startProcessing(1, CompanyStrategy("", StrategyEnum.SIMPLE_SCALPING))

        val chart = simpleScalpingStrategy.prepareChart(1L, reportSize)
        val stream = FileOutputStream(File(outputFileName))
        ChartUtils.writeChartAsPNG(
            stream,
            chart,
            1024,
            400
        )
    }
}