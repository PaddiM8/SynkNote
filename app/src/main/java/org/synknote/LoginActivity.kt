package org.synknote

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_login.*
import org.apache.commons.lang.RandomStringUtils
import org.synknote.algorithms.PBKDF2Algo
import org.synknote.files.Notebook
import org.synknote.files.NotebookData
import org.synknote.misc.getPref
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager
import org.synknote.sync.SyncManager
import com.google.gson.reflect.TypeToken
import android.content.Intent
import org.synknote.misc.fixUrl


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager(this).loadTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener { loginOnClick() }
    }

    private fun loginOnClick() {
        val email    = email_login_input.text.toString()
        val password = password_login_input.text.toString()

        val syncManager = SyncManager(this)
        val result = syncManager.login(email, password)

        if (!syncManager.resultIsError()) {
            val syncPref = PrefManager(this, PrefGroup.Sync)
            syncPref.setString("userId", result["id"].toString())
            syncPref.setString("token",  result["token"].toString())
            syncPref.setBoolean("logged_in", true)

            val newEncryptionKey = PBKDF2Algo.generateHash(password,
                    MainActivity.Protection.salt.toByteArray())
            MainActivity.Protection.encryptionKey = newEncryptionKey

            val result = syncManager.getAllNotebooks(result["id"].toString(),
                                                     result["token"].toString())

            val notebooks = GsonBuilder().create().fromJson(result["notebooks"].toString(),
                                              ArrayList<Map<String, Map<String, String>>>()::class.java)
            for (notebook in notebooks) {
                val location = fixUrl(filesDir.canonicalPath) + notebook["name"].toString()
                Notebook(this, notebook["name"].toString())
                        .add(location, true, notebook["id"].toString(), false)
                        .getNotes(notebook["id"].toString())
            }

            finish()

            val i = Intent(baseContext, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }
    }
}
