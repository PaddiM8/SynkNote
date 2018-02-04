package synknotecom.paddi.synknote

import android.annotation.SuppressLint
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.util.Log
import android.app.Activity



/**
* Created by PaddiM8 on 1/30/18.
*/

fun getFileExtensionFromType(type: String) : String {
    return if (type == "Markdown Document")
        ".md"
    else
        ".txt"
}

@SuppressLint("SimpleDateFormat")
fun getDate(milliSeconds: Long, dateFormat: String): String {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat)

    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

fun languageNameToLocale(languageName: String) : Locale {
    if (languageName == "Swedish")
        return Locale("se")
    else if (languageName == "English")
        return Locale("en")
    return Locale("en")
}

@SuppressLint("ObsoleteSdkInt")
fun setAppLocale(language: String, activity: Activity) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val resources = activity.resources
        val configuration = resources.configuration
        configuration.setLocale(Locale(language))
        activity.applicationContext.createConfigurationContext(configuration)
    } else {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = activity.resources.configuration
        config.locale = locale
        activity.resources.updateConfiguration(config,
                activity.resources.displayMetrics)
    }

}