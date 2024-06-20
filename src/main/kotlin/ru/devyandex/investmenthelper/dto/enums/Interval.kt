package ru.devyandex.investmenthelper.dto.enums

import java.time.Duration

/**
 * Набор интервалов с длительностью в минутах
 * @property minuteDuration - длительность в минутах
 */
enum class Interval(private val minuteDuration: Long) {
    INTERVAL_1_MIN(1),
    INTERVAL_5_MIN(5),
    INTERVAL_15_MIN(15),
    INTERVAL_HOUR(60),
    INTERVAL_DAY(1440),
    INTERVAL_2_MIN(2),
    INTERVAL_3_MIN(3),
    INTERVAL_10_MIN(10),
    INTERVAL_30_MIN(30),
    INTERVAL_2_HOUR(120),
    INTERVAL_4_HOUR(240),
    INTERVAL_WEEK(10080),
    INTERVAL_30_DAYS(43200);

    /**
     * Метод для получения java.time.Duration соответствующий полю minuteDuration
     */
    fun toDuration(): Duration = Duration.ofMinutes(this.minuteDuration)
}