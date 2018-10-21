package org.synknote

import android.content.Context
import org.synknote.Misc.ActivityTypes
import org.synknote.Misc.Icons
import org.synknote.Misc.getPref

class ThemeManager(context: Context, activityType: ActivityTypes = ActivityTypes.NORMAL) {
    private val _context = context
    private val _activityType = activityType
    private val _defaultTheme = R.style.AppTheme_Dark_Full

    init {
        if (!getPref("Themes", context).contains("currentTheme"))
             getPref("Themes", context).edit().putString("currentTheme", "darkFull").apply()
    }

    fun loadTheme() {
        val themeName = getPref("Themes", _context).getString("currentTheme", null)
        loadTheme(themeName)
    }

    private fun loadTheme(theme: Int) {
        _context.setTheme(getActivityTheme(theme))
    }

    private fun loadTheme(themeName: String) {
        loadTheme(themeNameToId(themeName))
    }

    fun getIcon(icon: Icons): Int {
        val themeName = getPref("Themes", _context).getString("currentTheme", null)
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
        }

        return 0
    }

    private fun themeNameToId(themeName: String): Int {
        return when (themeName) {
            "dark"        -> R.style.AppTheme_Dark
            "light"       -> R.style.AppTheme
            "darkFull"    -> R.style.AppTheme_Dark_Full
            "lightOrange" -> R.style.AppTheme_Orange
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