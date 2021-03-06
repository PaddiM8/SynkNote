package org.synknote.misc

import android.annotation.SuppressLint
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.apache.commons.lang.RandomStringUtils
import android.text.Selection
import android.widget.EditText
import org.apache.commons.lang.StringUtils
import org.synknote.algorithms.PBKDF2Algo
import org.synknote.MainActivity
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager
import java.io.File
import android.app.AlarmManager
import android.content.Intent
import android.app.PendingIntent
import android.util.Log
import org.synknote.models.NoteData


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
        "$url/"
    else
        url
}

fun isInMainDirectory(context: Context): Boolean {
    return MainActivity.FileList.currentDirectory == getSaveLocation(context)
}

fun getCurrentLinePosition(editText: EditText): Array<Int> {
    val selectionStart = Selection.getSelectionStart(editText.text)
    val layout = editText.layout

    return if (selectionStart != -1) {
        val lineNumber = layout.getLineForOffset(selectionStart)
        val lineStart = layout.getLineStart(lineNumber)
        val lineEnd = layout.getLineEnd(lineNumber)
        arrayOf(lineStart, lineEnd)
    } else arrayOf(-1, -1)
}

fun getSaveLocation(context: Context): String {
    return fixUrl(MainActivity.FileList.currentNotebook.location)
}


fun documentExists(documentName: String) : Boolean {
    val fileLocation = MainActivity.FileList.currentDirectory + documentName
    return File(fileLocation).exists()
}


fun isValidFileName(fileName: String): Boolean {
    return !StringUtils.containsAny(fileName, "|\\?*<\\\":>/'")
}

fun partitionNoteData(content: String): NoteData {
    val body = content.substring(0, content.length - 53)
    val dataString = content.substring(content.length - 53)
    val salt = dataString.substring(0, 29)
    val id = dataString.substring(29)
    Log.d("ID1", id)

    return NoteData(id, salt, body)
}