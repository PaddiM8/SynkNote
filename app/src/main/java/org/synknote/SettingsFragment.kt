package org.synknote


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceFragmentCompat
import android.content.Intent
import org.synknote.R


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themeButton = findPreference("change_theme_button")
        themeButton.setOnPreferenceClickListener {
            val themeChooser = Intent(context, Themes::class.java)
            startActivityForResult(themeChooser, 1)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1)
            activity!!.recreate()
    }
}
