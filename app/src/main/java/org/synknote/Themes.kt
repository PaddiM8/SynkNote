package org.synknote

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_themes.*
import org.synknote.misc.getPref
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager

class Themes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager(this).loadTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_themes)

        val themesPref = PrefManager(this, PrefGroup.Themes)
        dark_theme_item.setOnClickListener {
            themesPref.setString("current_theme", "dark")
            setResult(1)
            finish()
        }

        light_theme_item.setOnClickListener {
            themesPref.setString("current_theme", "light")
            setResult(1)
            finish()
        }

        dark_full_item.setOnClickListener {
            themesPref.setString("current_theme", "dark_full")
            setResult(1)
            finish()
        }

        orange_item.setOnClickListener {
            themesPref.setString("current_theme", "light_orange")
            setResult(1)
            finish()
        }
    }
}
