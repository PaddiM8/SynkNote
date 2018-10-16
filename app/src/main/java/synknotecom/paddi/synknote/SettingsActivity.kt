package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import synknotecom.paddi.synknote.Algorithms.PBKDF2Algo
import java.io.File

class SettingsActivity : AppCompatActivity() {

    object OnStart {
        var localFolderLocation: String = ""
    }

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

        // Save current set local folder location, to later see if it was changed
        OnStart.localFolderLocation = getDefaultPref(this)
                                        .getString("localFolderEditText", null)

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener { _: SharedPreferences, tag: String ->
            when (tag) {
                "darkThemeSettingsCheckbox" -> this.recreate()
                /*"passwordLockEditText" -> {
                    val defaultPref = getDefaultPref(this)
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
                }*/
                "passwordLockSwitch" -> {
                    val newKey = getPref("Security", this).getString("defaultEncryptionKey", null)

                    if (!getDefaultPref(this).getBoolean("passwordLockSwitch", false)) {
                        reencryptDocuments(File(getSaveLocation(this)),
                                           MainActivity.Protection.encryptionKey,
                                           getPref("Security", this).getString("defaultEncryptionKey", null))

                        MainActivity.Protection.encryptionKey = newKey
                        getPref("DataPref", this).edit().putString("password_hash", newKey).apply()
                    }
                }
                "localFolderEditText" -> {
                    //val input = getDefaultPref(this).getString("localFolderEditText", null)
                    //if (!input.endsWith("/"))
                    //    getDefaultPref(this).edit().putString("localFolderEditText", input + "/").apply()
                }
            }

        }
    }

    private fun reencryptDocuments(folder: File, oldKey: String, newKey: String) {
        val files = folder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    Log.d("Decrypting", file.readText().decrypt(oldKey).first)
                    val lastModified = file.lastModified()
                    val decryptedFileContent = file.readText().decrypt(oldKey).first // Decrypt file with old key
                    file.writeText(decryptedFileContent.encrypt(newKey)) // Encrypt file with new key
                    file.setLastModified(lastModified)
                } else {
                    reencryptDocuments(file, oldKey, newKey)
                }
            }
        }
    }

    private fun applySettings() {

        // Check if folder location was changed
        val folderLocation = getDefaultPref(this).getString("localFolderEditText", null)
        if (OnStart.localFolderLocation != folderLocation)
            MainActivity.FileList.currentDirectory = folderLocation // Change current directory to the new folder location

        applyPasswordLockHash()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        MainActivity.Protection.askForPassword = false
    }

    private fun applyPasswordLockHash() {
        val defaultPref = getDefaultPref(this)
        val specifiedPassword = defaultPref.getString("passwordLockEditText", "")

        if (defaultPref.getBoolean("passwordLockSwitch", false) && specifiedPassword != "") {
            val hashedPassword = PBKDF2Algo.generateHash(specifiedPassword, MainActivity.Protection.salt.toByteArray())
            val pref = applicationContext.getSharedPreferences("DataPref", Context.MODE_PRIVATE)

            if (hashedPassword != MainActivity.Protection.encryptionKey) {
                MainActivity.Protection.encryptionKey = hashedPassword
                reencryptDocuments(File(getSaveLocation(this)),
                                   pref.getString("password_hash", null),
                                   hashedPassword)
            }

            pref.edit().putString("password_hash", hashedPassword).apply()
            defaultPref.edit().putString("passwordLockEditText", "").apply()
        }
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
