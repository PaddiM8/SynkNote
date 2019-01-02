package org.synknote

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import org.synknote.misc.getPref
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager
import org.synknote.sync.SyncManager

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager(this).loadTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        button_register.setOnClickListener { registerOnClick() }
    }

    private fun registerOnClick() {
        val email    = email_register_input.text.toString()
        val password = password_register_input.text.toString()
        val confirm  = confirm_password_register_input.text.toString()

        if (password != confirm) {
            Toast.makeText(this, "Passwords don't match.", Toast.LENGTH_SHORT).show()
        }

        val syncManager = SyncManager(this)
        val result = syncManager.register(email, password)

        if (!syncManager.resultIsError()) { // TODO only do this if valid result
            Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
