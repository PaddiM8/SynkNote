package synknotecom.paddi.synknote

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import java.text.SimpleDateFormat
import java.util.*


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