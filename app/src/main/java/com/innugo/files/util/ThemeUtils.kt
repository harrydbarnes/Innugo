package com.innugo.files.util

import java.util.Calendar

object ThemeUtils {
    /** Returns true if the current hour is before 06:00 or at/after 20:00. */
    fun shouldUseDarkTheme(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 20
    }
}
