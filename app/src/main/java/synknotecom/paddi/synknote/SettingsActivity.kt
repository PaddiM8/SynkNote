package synknotecom.paddi.synknote

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem

class SettingsActivity : AppCompatActivity() {

    object Settings {
        var darkTheme = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.elevation = 0f
        }

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
        val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this)

        Settings.darkTheme = sharedPref.getBoolean("darkThemeCheckbox", false)
    }

    private fun applySettings() {
        val intent = Intent(this, MainWindow::class.java)
        startActivity(intent)
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
