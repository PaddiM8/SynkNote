package synknotecom.paddi.synknote

import android.annotation.SuppressLint
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.apache.commons.lang.RandomStringUtils


/**
* Created by PaddiM8 on 1/30/18.
*/

fun getFileExtensionFromType(type: String): String {
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

fun loadTheme(context: Context) {
    val defaultPref = PreferenceManager.getDefaultSharedPreferences(context)
    if (defaultPref.getBoolean("darkThemeSettingsCheckbox", false))
        context.setTheme(R.style.AppTheme_Dark)
}

fun getDefaultPref(context: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
}

fun getPref(key: String, context: Context): SharedPreferences {
    return context.getSharedPreferences(key, AppCompatActivity.MODE_PRIVATE)
}

fun showSoftwareKeyboard(show: Boolean, view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (show)
        imm.showSoftInput(view, 0)
    else
        imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun generateRandomEncryptionKey(): String {
    return PBKDF2Algo.generateHash(RandomStringUtils.randomAlphanumeric(32),
            MainActivity.Protection.salt.toByteArray())
}

fun fixUrl(url: String): String {
    return if (!url.endsWith("/"))
        url + "/"
    else
        url
}

fun isInMainDirectory(context: Context): Boolean {
    return MainActivity.FileList.currentDirectory == getSaveLocation(context)
}