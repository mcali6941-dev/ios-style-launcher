package com.mohamed.iosstylelauncher

import android.content.Context

data class LauncherConfig(
    val wallpaper: String = "ocean",
    val columns: Int = 4,
    val iconSize: Int = 58,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showLabels: Boolean = true,
    val glassDock: Boolean = true
)

object LauncherPreferences {

    private const val PREFS = "maxamed_cali_ios_settings"

    fun load(context: Context): LauncherConfig {
        val prefs = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

        return LauncherConfig(
            wallpaper = prefs.getString(
                "wallpaper",
                "ocean"
            ) ?: "ocean",

            columns = prefs.getInt("columns", 4),

            iconSize = prefs.getInt("icon_size", 58),

            showClock = prefs.getBoolean(
                "show_clock",
                true
            ),

            showDate = prefs.getBoolean(
                "show_date",
                true
            ),

            showLabels = prefs.getBoolean(
                "show_labels",
                true
            ),

            glassDock = prefs.getBoolean(
                "glass_dock",
                true
            )
        )
    }

    fun save(
        context: Context,
        config: LauncherConfig
    ) {
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).edit()
            .putString("wallpaper", config.wallpaper)
            .putInt("columns", config.columns)
            .putInt("icon_size", config.iconSize)
            .putBoolean("show_clock", config.showClock)
            .putBoolean("show_date", config.showDate)
            .putBoolean("show_labels", config.showLabels)
            .putBoolean("glass_dock", config.glassDock)
            .apply()
    }
}
