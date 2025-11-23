package com.personaljournal.util

import kotlinx.datetime.Instant

private const val MILLIS_IN_SECOND = 1_000
private const val NANOS_IN_MILLI = 1_000_000

fun Instant.toEpochMillisCompat(): Long =
    epochSeconds * MILLIS_IN_SECOND + nanosecondsOfSecond / NANOS_IN_MILLI
