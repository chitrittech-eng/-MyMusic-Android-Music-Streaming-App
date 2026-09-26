package com.mymusic.app.core.common

import java.util.concurrent.TimeUnit

fun Long.toReadableDuration(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(minutes)
    return "%d:%02d".format(minutes, seconds)
}

fun Int.toReadableDuration(): String = this.toLong().toReadableDuration()

fun Long.formatStreamCount(): String = when {
    this >= 1_000_000_000 -> "%.1fB".format(this / 1_000_000_000.0)
    this >= 1_000_000 -> "%.1fM".format(this / 1_000_000.0)
    this >= 1_000 -> "%.1fK".format(this / 1_000.0)
    else -> this.toString()
}

fun String.toInitials(): String =
    split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
