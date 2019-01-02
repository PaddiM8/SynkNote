package org.synknote

import android.content.Context
import org.synknote.misc.ActivityTypes
import org.synknote.misc.Icons
import org.synknote.misc.getPref
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager

class ThemeManager(context: Context, activityType: ActivityTypes = ActivityTypes.NORMAL) {
    private val _context = context
    private val _activityType = activityType
    private val _defaultTheme = R.style.AppTheme_Dark_Full

    init {
        val themesPref = PrefManager(context, PrefGroup.Themes)
        if (!themesPref.get().contains("current_theme"))
            themesPref.setString("current_theme", "dark_full")
    }

    fun loadTheme() {
        val themeName = PrefManager(_context, PrefGroup.Themes).getString("current_theme")
        loadTheme(themeName)
    }

    private fun loadTheme(theme: Int) {
        _context.setTheme(getActivityTheme(theme))
    }

    private fun loadTheme(themeName: String) {
        loadTheme(themeNameToId(themeName))
    }

    fun getIcon(icon: Icons): Int {
        val themeName = PrefManager(_context, PrefGroup.Themes).getString("current_theme")
        if (icon == Icons.FOLDER) {
            return when {
                themeName.startsWith("light")     -> R.drawable.ic_folder
                themeName.startsWith("dark")      -> R.drawable.ic_folder_dark
                else    -> R.drawable.ic_folder_dark
            }
        } else if (icon == Icons.FILE) {
            return when {
                themeName.startsWith("light")     -> R.drawable.ic_file
                themeName.startsWith("dark")      -> R.drawable.ic_file_dark
                else    -> R.drawable.ic_file_dark
            }
        } else if (icon == Icons.NOTEBOOK) {
            return when {
                themeName.startsWith("light")     -> R.drawable.ic_notebook_light
                themeName.startsWith("dark")      -> R.drawable.ic_notebook_dark
                else    -> R.drawable.ic_notebook_light
            }
        }

        return 0
    }

    private fun themeNameToId(themeName: String): Int {
        return when (themeName) {
            "dark"        -> R.style.AppTheme_Dark
            "light"       -> R.style.AppTheme
            "dark_full"    -> R.style.AppTheme_Dark_Full
            "light_orange" -> R.style.AppTheme_Orange
            else          -> _defaultTheme
        }
    }

    private fun getActivityTheme(theme: Int): Int {
        if (_activityType == ActivityTypes.NORMAL) return theme

        if (_activityType == ActivityTypes.MAIN) {
            return when (theme) {
                R.style.AppTheme_Dark -> R.style.AppTheme_Dark_Main
                R.style.AppTheme -> R.style.AppTheme_Main
                R.style.AppTheme_Dark_Full -> R.style.AppTheme_Dark_Full_Main
                R.style.AppTheme_Orange -> R.style.AppTheme_Orange_Main
                else                       -> R.style.AppTheme_Dark_Full_Main
            }
        }

        if (_activityType == ActivityTypes.PREFERENCES) {
            return when (theme) {
                R.style.AppTheme_Dark -> R.style.AppTheme_SettingsTheme_Dark
                R.style.AppTheme -> R.style.AppTheme_SettingsTheme
                R.style.AppTheme_Dark_Full -> R.style.AppTheme_SettingsTheme_Dark_Full
                R.style.AppTheme_Orange -> R.style.AppTheme_SettingsTheme_Orange
                else                       -> R.style.AppTheme_SettingsTheme_Dark_Full
            }
        }

        if (_activityType == ActivityTypes.EDITOR) {
            return when (theme) {
                R.style.AppTheme_Dark -> R.style.AppTheme_Dark_General
                R.style.AppTheme -> R.style.AppTheme_General
                R.style.AppTheme_Dark_Full -> R.style.AppTheme_Dark_Full_General
                R.style.AppTheme_Orange -> R.style.AppTheme_Orange_General
                else                       -> R.style.AppTheme_Dark_Full_General
            }
        }

        return theme
    }
}