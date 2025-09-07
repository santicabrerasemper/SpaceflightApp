package com.example.spaceflightapp.core.utils.helpers

import java.time.*
import java.time.format.*
import java.util.Locale
import kotlin.math.abs

object DateFmt {
    private fun locale() = Locale.getDefault()
    private fun zone() = ZoneId.systemDefault()

    private val cardBase: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy • HH:mm", locale())

    private fun compactGmt(zdt: ZonedDateTime): String {
        val totalMin = zdt.offset.totalSeconds / 60
        val h = abs(totalMin / 60)
        val m = abs(totalMin % 60)
        val sign = if (totalMin >= 0) "+" else "-"

        return if (m == 0) "GMT$sign$h" else String.format(locale(), "GMT%s%02d:%02d", sign, h, m)
    }

    fun formatIsoForCard(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            val inst = try { Instant.parse(iso) } catch (_: Exception) { OffsetDateTime.parse(iso).toInstant() }
            val zdt = inst.atZone(zone())
            "${zdt.format(cardBase)} ${compactGmt(zdt)}"
        } catch (_: Exception) { iso }
    }

}
