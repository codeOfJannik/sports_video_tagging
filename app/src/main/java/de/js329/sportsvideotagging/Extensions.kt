package de.js329.sportsvideotagging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import java.time.Duration
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.math.abs

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Long.toTimeOffsetString(): String {
    val timeOffsetSign = if (this >= 0) "+" else "-"
    val minSecPair = this.toMinutesAndSecond()
    return String.format(Locale.getDefault(), "%s%02d:%02d", timeOffsetSign, minSecPair.first, minSecPair.second)
}

fun Long.toMinutesAndSecond(): Pair<Long, Long> {
    Duration.ofSeconds(abs(this)).also {
        val minutes = it.toMinutes()
        val seconds = it.minusMinutes(minutes).seconds
        return Pair(minutes, seconds)
    }
}

fun Duration.toHHMMSSString(): String {
    val hours = this.toHours()
    val minutes = this.minusHours(hours).toMinutes()
    val seconds = this.minusHours(hours).minusMinutes(minutes).seconds
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

fun Calendar.toFormattedString(): String {
    return String.format(
        Locale.getDefault(),
        "%02d. %s %d",
        this.get(Calendar.DAY_OF_MONTH),
        Month.of(this.get(Calendar.MONTH)).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
        this.get(Calendar.YEAR)
    )
}