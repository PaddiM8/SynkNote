package synknotecom.paddi.synknote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
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
import java.io.File

class Editor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        var actionBar = supportActionBar
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true)

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

        textEditor.setOnFocusChangeListener { view: View, b: Boolean ->
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

        var id = item.itemId
        if (id == android.R.id.home) {
            val intent = Intent(this, MainWindow::class.java)
            showSoftwareKeyboard(false)
            startActivity(intent)
        }

        return true
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

    /*private fun saveDocument() {
        val fileName = intent.getStringExtra("filename") //title.toString() + getFileExtensionFromType(documentType)
        val fos = openFileOutput(fileName, Context.MODE_PRIVATE)
        fos.write(markdownEditor.text.toString().toByteArray())
        fos.close()

        Toast.makeText(this,"Document saved.", Toast.LENGTH_SHORT).show();
    }*/

}
