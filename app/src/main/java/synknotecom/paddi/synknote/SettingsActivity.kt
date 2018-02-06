package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import java.io.File

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("darkThemeSettingsCheckbox", false))
            setTheme(R.style.AppTheme_SettingsTheme_Dark)

        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.elevation = 0f
        }

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, tag: String ->
            when (tag) {
                "darkThemeSettingsCheckbox" -> this.recreate()
                "passwordLockEditText" -> {
                    val defaultPref = getDefaultPref(this)!!
                    val specifiedPassword = defaultPref.getString("passwordLockEditText", "")

                    if (defaultPref.getBoolean("passwordLockSwitch", false) && specifiedPassword != "") {
                        val hashedPassword = PBKDF2Algo.generateHash(specifiedPassword, MainActivity.Protection.salt.toByteArray())
                        val pref = applicationContext.getSharedPreferences("DataPref", Context.MODE_PRIVATE)

                        pref.edit().putString("password_hash", hashedPassword).apply()
                        defaultPref.edit().putString("passwordLockEditText", "").apply()

                        val newKey = PBKDF2Algo.generateHash(specifiedPassword, MainActivity.Protection.salt.toByteArray())
                        MainActivity.Protection.encryptionKey = newKey
                        reencryptDocuments(getPref("Security", this).getString("defaultEncryptionKey", null),
                                newKey)
                    }
                }
                "passwordLockSwitch" -> {
                    var newKey = getPref("Security", this).getString("defaultEncryptionKey", null)

                    if (!getDefaultPref(this)!!.getBoolean("passwordLockSwitch", false)) {
                        reencryptDocuments(MainActivity.Protection.encryptionKey,
                                getPref("Security", this).getString("defaultEncryptionKey", null))

                        MainActivity.Protection.encryptionKey = newKey
                        getPref("DataPref", this).edit().putString("password_hash", newKey).apply()
                    }
                }
            }

        }
    }

    private fun reencryptDocuments(oldKey: String, newKey: String) {
        for (file in File(applicationContext.applicationInfo.dataDir + "/files/").listFiles()) {
            val decryptedFileContent = decryptString(file.readText(), oldKey) // Decrypt file with old key
            file.writeText(encryptString(decryptedFileContent, newKey)) // Encrypt file with new key
        }
    }

    private fun applySettings() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        MainActivity.Protection.askForPassword = false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        applySettings()
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == android.R.id.home)
            applySettings()
        return true
    }
}
