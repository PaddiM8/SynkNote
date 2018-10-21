package synknotecom.paddi.synknote

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jasypt.util.text.BasicTextEncryptor

/**
* Created by PaddiM8 on 1/31/18.
*/

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun <T: RecyclerView.ViewHolder> T.onClick(event: (position: Int, type: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(adapterPosition, itemViewType)
    }

    return this
}

fun Boolean.toInt() =
        if (this) 1 else 0

fun Int.binaryInvert() =
        if (this == 0) 1 else 0

fun String.encrypt(key: String): String {
    return try {
        val textEncryptor = BasicTextEncryptor()
        textEncryptor.setPassword(key)
        Log.d("Encryption Key", key)
        textEncryptor.encrypt(this)
    } catch (e: Exception) {
        ""
    }
}

fun String.decrypt(key: String): Pair<String, Boolean> {
    return try {
        val textEncryptor = BasicTextEncryptor()
        textEncryptor.setPassword(key)
        Log.d("Decryption Key", key)
        Pair(textEncryptor.decrypt(this), true)
    } catch (e: Exception) {
        Pair("", false)
    }
}