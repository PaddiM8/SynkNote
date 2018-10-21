package synknotecom.paddi.synknote


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceFragmentCompat
import android.R.attr.button
import android.content.Intent
import android.support.v7.preference.Preference
import synknotecom.paddi.synknote.R.layout.activity_themes


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themeButton = findPreference("changeThemeButton")
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
