package org.synknote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import org.synknote.Algorithms.PBKDF2Algo
import org.synknote.Misc.*
import java.io.File

class SettingsActivity : AppCompatActivity() {

    object OnStart {
        var localFolderLocation: String = ""
        var encryptOption: Boolean = true
        var passwordLock: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager(this, ActivityTypes.PREFERENCES).loadTheme()

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

        OnStart.encryptOption = getDefaultPref(this)
                .getBoolean("encryptFilesSwitch", false)

        OnStart.passwordLock = getDefaultPref(this)
                .getBoolean("passwordLockSwitch", false)
    }

    private fun reencryptDocuments(folder: File, oldKey: String, newKey: String) {
        val files = folder.listFiles() ?: return

        for (file in files) {
            if (file.isFile) {
                val lastModified = file.lastModified()

                // Decrypt, unless the original is raw text
                var decryptedFileContent = file.readText()
                if (oldKey != "") {
                    decryptedFileContent = decryptedFileContent.decrypt(oldKey).first // Decrypt file with old key
                }

                // Encrypt unless it's supposed to only be decrypted
                var encryptedFileContent = decryptedFileContent
                if (newKey != "") {
                    encryptedFileContent = encryptedFileContent.encrypt(newKey)
                }

                file.writeText(encryptedFileContent) // Encrypt file with new key
                file.setLastModified(lastModified)
            } else {
                reencryptDocuments(file, oldKey, newKey)
            }
        }
    }

    private fun applySettings() {

        // Check if folder location was changed
        val folderLocation = getDefaultPref(this).getString("localFolderEditText", null)
        if (OnStart.localFolderLocation != folderLocation)
            MainActivity.FileList.currentDirectory = folderLocation // Change current directory to the new folder location

        if (getDefaultPref(this).getBoolean("encryptFilesSwitch", false) != OnStart.encryptOption)
            onEncryptOptionChanged()

        if (getDefaultPref(this).getBoolean("passwordLockSwitch", false) != OnStart.passwordLock)
            applyPasswordLockHash()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        MainActivity.Protection.askForPassword = false
    }

    private fun onEncryptOptionChanged() {
        val defaultPref = getDefaultPref(this)
        val encrypt     = defaultPref.getBoolean("encryptFilesSwitch", false)
        val defaultEncryptionKey = getPref("Security", this)
                .getString("defaultEncryptionKey", null)

        if (encrypt) {
            reencryptDocuments(
                    File(getSaveLocation(this)),
                    "",
                    defaultEncryptionKey
            )

            MainActivity.Protection.encryptionKey = defaultEncryptionKey
        } else {
            reencryptDocuments(
                    File(getSaveLocation(this)),
                    MainActivity.Protection.encryptionKey,
                    ""
            )

            MainActivity.Protection.encryptionKey = defaultEncryptionKey
        }
    }

    private fun applyPasswordLockHash() {
        val defaultPref       = getDefaultPref(this)
        val specifiedPassword = defaultPref.getString("passwordLockEditText", "")
        val passwordLock      = defaultPref.getBoolean("passwordLockSwitch", false)
        val pref = applicationContext.getSharedPreferences("Security", Context.MODE_PRIVATE)

        if (passwordLock && specifiedPassword != "") {
            val hashedPassword = PBKDF2Algo.generateHash(specifiedPassword, MainActivity.Protection.salt.toByteArray())


            if (hashedPassword != MainActivity.Protection.encryptionKey) {
                MainActivity.Protection.encryptionKey = hashedPassword
                reencryptDocuments(File(getSaveLocation(this)),
                                   pref.getString("password_hash", null),
                                   hashedPassword)
            }

            pref.edit().putString("password_hash", hashedPassword).apply()
        } else if (!passwordLock) {
            val defaultEncryptionKey = pref.getString("defaultEncryptionKey", null)
            reencryptDocuments(File(getSaveLocation(this)),
                    pref.getString("password_hash", null),
                    defaultEncryptionKey)

            MainActivity.Protection.encryptionKey = defaultEncryptionKey
            pref.edit().putString("password_hash", defaultEncryptionKey).apply()

        }

        defaultPref.edit().putString("passwordLockEditText", "").apply()
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
