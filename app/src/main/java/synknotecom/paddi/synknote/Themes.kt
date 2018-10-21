package synknotecom.paddi.synknote

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main_window.*
import kotlinx.android.synthetic.main.activity_themes.*

class Themes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager(this).loadTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_themes)

        darkThemeItem.setOnClickListener {
            getPref("Themes", this).edit().putString("currentTheme", "dark").apply()
            setResult(1)
            finish()
        }

        lightThemeitem.setOnClickListener {
            getPref("Themes", this).edit().putString("currentTheme", "light").apply()
            setResult(1)
            finish()
        }

        darkFullItem.setOnClickListener {
            getPref("Themes", this).edit().putString("currentTheme", "darkFull").apply()
            setResult(1)
            finish()
        }

        orangeItem.setOnClickListener {
            getPref("Themes", this).edit().putString("currentTheme", "lightOrange").apply()
            setResult(1)
            finish()
        }
    }
}
