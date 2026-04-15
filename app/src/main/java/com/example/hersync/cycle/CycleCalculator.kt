package com.example.hersync.cycle

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class CyclePrediction(
    val nextPeriodStart: LocalDate,
    val daysUntilNextPeriod: Long,
    val fertileStart: LocalDate,
    val fertileEnd: LocalDate,
    val ovulationDate: LocalDate,
)

enum class CyclePhase {
    Menstrual,
    Follicular,
    Ovulation,
    Luteal,
}

object CycleCalculator {

    fun predict(
        lastPeriodStart: LocalDate,
        cycleLengthDays: Int,
        today: LocalDate = LocalDate.now(),
    ): CyclePrediction {
        val cycle = cycleLengthDays.coerceAtLeast(1)
        val next = lastPeriodStart.plusDays(cycle.toLong())
        val ovulation = next.minusDays(14)
        val fertileStart = ovulation.minusDays(4)
        val fertileEnd = ovulation.plusDays(1)
        val daysUntil = ChronoUnit.DAYS.between(today, next)
        return CyclePrediction(
            nextPeriodStart = next,
            daysUntilNextPeriod = daysUntil,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            ovulationDate = ovulation,
        )
    }

    /** 1-based day within the current cycle, or null if before lastPeriodStart. */
    fun cycleDay(
        lastPeriodStart: LocalDate,
        today: LocalDate,
        cycleLength: Int,
    ): Int? {
        val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, today).toInt()
        if (daysBetween < 0) return null
        val mod = daysBetween % cycleLength
        return mod + 1
    }

    /** 1-based cycle day of ovulation (~14 days before next period). */
    fun ovulationCycleDay(cycleLength: Int): Int =
        (cycleLength - 13).coerceIn(1, cycleLength)

    fun phaseForCycleDay(
        cycleDay: Int,
        cycleLength: Int,
        periodDuration: Int,
    ): CyclePhase {
        val pd = periodDuration.coerceIn(1, cycleLength)
        val ovDay = ovulationCycleDay(cycleLength)
        val ovStart = (ovDay - 1).coerceAtLeast(1)
        val ovEnd = (ovDay + 2).coerceAtMost(cycleLength)

        return when {
            cycleDay <= pd -> CyclePhase.Menstrual
            cycleDay < ovStart -> CyclePhase.Follicular
            cycleDay in ovStart..ovEnd -> CyclePhase.Ovulation
            else -> CyclePhase.Luteal
        }
    }

    fun shortDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))

    fun shortRange(start: LocalDate, end: LocalDate): String {
        val m = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        return "${start.format(m)}–${end.format(m)}"
    }

}
