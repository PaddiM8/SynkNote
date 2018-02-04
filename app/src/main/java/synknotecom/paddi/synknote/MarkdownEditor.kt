package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_editor.*
import ru.noties.markwon.Markwon

class MarkdownEditor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkThemeSettingsCheckbox", false))
            setTheme(R.style.AppTheme_Dark_General)
        else
            setTheme(R.style.AppTheme_General)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.elevation = 0f
        }

        title = intent.getStringExtra("title")
        markdownEditor.setText(intent.getStringExtra("content"))

        markdownEditor.setOnClickListener {
            if (markdownEditor.hasFocus())
                showSoftwareKeyboard(true)
        }

        boldButton.setOnClickListener {
            val documentString = SpannableStringBuilder(textEditor.text.toString())
            documentString.setSpan(StyleSpan(Typeface.BOLD), textEditor.selectionStart, textEditor.selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            TextView(textEditor.context).text = documentString
        }

        textEditor.setOnFocusChangeListener { view: View, _: Boolean ->
            if (textEditor.hasFocus()) {
                Markwon.setMarkdown(textEditor, markdownEditor.text.toString())
                showSoftwareKeyboard(false, view)
            }
        }

        editorTabs.setup()

        // Tab 1
        val spec = editorTabs.newTabSpec("Markdown")
        spec.setContent(markdownView.id)
        spec.setIndicator("Markdown")
        editorTabs.addTab(spec)

        // Tab 2
        val spec2 = editorTabs.newTabSpec("Preview")
        spec2.setContent(documentView.id)
        spec2.setIndicator("Preview")
        editorTabs.addTab(spec2)

    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == android.R.id.home)
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

    private fun showSoftwareKeyboard(show: Boolean, view: View = View(this)) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (show)
            imm.showSoftInput(markdownEditor, 0)
        else
            imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        saveDocument(applicationContext, intent.getStringExtra("filename"), markdownEditor)
        Toast.makeText(this,"Document saved.", Toast.LENGTH_SHORT).show()
    }

}
