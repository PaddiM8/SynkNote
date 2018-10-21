package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.activity_main_window.*
import kotlinx.android.synthetic.main.activity_normal_editor.*
import synknotecom.paddi.synknote.Files.Document

class NormalEditor : AppCompatActivity() {

    private var editedSinceLastSave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager(this, ActivityTypes.EDITOR).loadTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_editor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = intent.getStringExtra("title")
        normal_text_editor.setText(intent.getStringExtra("content"))
        autoSave()

        normal_text_editor.setOnClickListener {
            if (normal_text_editor.hasFocus())
                showSoftwareKeyboard(true)
        }

        normal_text_editor.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editedSinceLastSave = true
            }
        })

    }

    private fun autoSave() {
        val handler = Handler()
        val delay = 5000L //  Milliseconds

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (editedSinceLastSave) {
                    Document().save(applicationContext, intent.getStringExtra("filename"), normal_text_editor)
                    editedSinceLastSave = false
                }

                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        super.onOptionsItemSelected(item)

        val id = item.itemId
        if (id == android.R.id.home)
            onBack()

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBack()
    }

    private fun onBack() {
        val intent = Intent(this, MainActivity::class.java)
        showSoftwareKeyboard(false)
        startActivity(intent)
        MainActivity.Protection.askForPassword = false
    }

    override fun onPause() {
        super.onPause()
        Document().save(applicationContext, intent.getStringExtra("filename"), normal_text_editor)
        Toast.makeText(this,"Document saved.", Toast.LENGTH_SHORT).show()
    }

    private fun showSoftwareKeyboard(show: Boolean, view: View = View(this)) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (show)
            imm.showSoftInput(normal_text_editor, 0)
        else
            imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
