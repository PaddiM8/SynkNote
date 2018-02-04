package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

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
                    val defaultPref = PreferenceManager.getDefaultSharedPreferences(this)
                    val specifiedPassword = defaultPref.getString("passwordLockEditText", "")

                    if (specifiedPassword != "") {
                        val hashedPassword = BCrypt.hashpw(specifiedPassword, BCrypt.gensalt())
                        val pref = applicationContext.getSharedPreferences("DataPref", Context.MODE_PRIVATE)

                        pref.edit().putString("password_hash", hashedPassword).apply()
                        defaultPref.edit().putString("passwordLockEditText", "").apply()
                        MainActivity.Protection.encryptionKey = PBKDF2Algo.generateHash(specifiedPassword, "salt".toByteArray())
                    }
                }
            }

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
