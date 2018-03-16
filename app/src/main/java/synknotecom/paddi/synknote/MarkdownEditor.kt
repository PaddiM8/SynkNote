package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.onegravity.rteditor.RTManager
import com.onegravity.rteditor.api.RTApi
import com.onegravity.rteditor.api.RTMediaFactoryImpl
import com.onegravity.rteditor.api.RTProxyImpl
import com.onegravity.rteditor.effects.Effects.ALL_EFFECTS
import kotlinx.android.synthetic.main.activity_editor.*
import org.apache.commons.lang.StringUtils.ordinalIndexOf
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

        // Create RTManager
        val rtApi = RTApi(this, RTProxyImpl(this), RTMediaFactoryImpl(this, true))
        val rtManager = RTManager(rtApi, savedInstanceState)

        rtManager.registerEditor(markdown_editor, true)

        title = intent.getStringExtra("title")
        markdown_editor.setText(intent.getStringExtra("content"))
        styleMarkdown(0, markdown_editor.text.length - 1)

        markdown_editor.setOnClickListener {
            if (markdown_editor.hasFocus())
                showSoftwareKeyboard(true)
        }

        text_editor.setOnFocusChangeListener { view: View, _: Boolean ->
            if (text_editor.hasFocus()) {
                Markwon.setMarkdown(text_editor, markdown_editor.text.toString())
                showSoftwareKeyboard(false, view)
            }
        }

        markdown_editor.addTextChangedListener(object:TextWatcher {
            var linePointBeforeChange: Array<Int> = emptyArray()
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {
                linePointBeforeChange = getCurrentLinePosition(markdown_editor)
            }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLinePoint = getCurrentLinePosition(markdown_editor)
                if (p0!!.count() == 1)
                    styleMarkdown(currentLinePoint[0], currentLinePoint[1])
                else if (p0.count() > 1)
                    styleMarkdown(linePointBeforeChange[0], start + count)
            }
        })

        editorTabs.setup()

        // Tab 1
        val spec = editorTabs.newTabSpec("Markdown")
        spec.setContent(markdown_view.id)
        spec.setIndicator("Markdown")
        editorTabs.addTab(spec)

        // Tab 2
        val spec2 = editorTabs.newTabSpec("Preview")
        spec2.setContent(document_view.id)
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

    fun styleMarkdown(start: Int, end: Int) {
        val markdown = markdown_editor.text.substring(start, end)
        val lexData = lexMarkdown(markdown) // Current line
        for ((typeIndex, modifier) in lexData.withIndex()) {
            var i = 0
            while (i + 1 < modifier.count()) {
                val index1 = start + modifier[i] + getType(typeIndex).length
                val index2 = start + modifier[i + 1]
                makeSelection(index1, index2, typeIndex)
                i += 2
            }
        }

        // Headings

        when {
            markdown.startsWith("# ")   -> makeSelection(start, end, 4)
            markdown.startsWith("## ")  -> makeSelection(start, end, 5)
            markdown.startsWith("### ") -> makeSelection(start, end, 6)
        }
    }

    private fun makeSelection(index1: Int, index2: Int, typeIndex: Int) {
        val cursorPosition = markdown_editor.selectionEnd
        val effectIndex = typeIndexToEffectIndex(typeIndex)
        val effectValues: List<*> = getEffectValues(typeIndex)

        // Set style to text inside modifiers
        markdown_editor.setSelection(index1, index2)
        ALL_EFFECTS[effectIndex].applyToSelection(markdown_editor, effectValues[0])

        // Gray out modifiers
        val typeLength = getType(typeIndex).length
        if (typeIndex < 4) {
            markdown_editor.setSelection(index1 - typeLength, index1)
            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.parseColor("#A6000000"))
            markdown_editor.setSelection(index2, index2 + typeLength)
            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.parseColor("#A6000000"))
            markdown_editor.setSelection(index2 + typeLength)

            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.BLACK)
            markdown_editor.setSelection(index1)
            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.BLACK)
            markdown_editor.setSelection(cursorPosition)
        } else {
            markdown_editor.setSelection(index1, index1 + typeLength)
            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.parseColor("#A6000000"))

            markdown_editor.setSelection(index1 + typeLength)
            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.BLACK)
            markdown_editor.setSelection(cursorPosition)
        }

        // Reset surroundings
        markdown_editor.setSelection(index2)
        ALL_EFFECTS[effectIndex].applyToSelection(markdown_editor, effectValues[1])
    }

    private fun typeIndexToEffectIndex(typeIndex: Int): Int {
        return arrayOf(0, 2, 1, 1, 6, 6, 6)[typeIndex]
    }

    private fun getType(typeIndex: Int): String {
        return arrayOf("**", "__", "*", "_", "#", "##", "###")[typeIndex]
    }

    private inline fun <reified T> getEffectValues(typeIndex: Int): T {
        return when (typeIndex) {
            0, 1, 2, 3 -> listOf(true, false) as T
            4 -> listOf(90, 24) as T
            5 -> listOf(75, 24) as T
            6 -> listOf(65, 24) as T
            else -> listOf(true, false) as T
        }
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
            imm.showSoftInput(markdown_editor, 0)
        else
            imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        saveDocument(applicationContext, intent.getStringExtra("filename"), markdown_editor)
        Toast.makeText(this,"Document saved.", Toast.LENGTH_SHORT).show()
    }

}
