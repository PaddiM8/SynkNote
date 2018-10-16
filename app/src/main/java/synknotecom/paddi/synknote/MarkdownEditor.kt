package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.onegravity.rteditor.RTManager
import com.onegravity.rteditor.api.RTApi
import com.onegravity.rteditor.api.RTMediaFactoryImpl
import com.onegravity.rteditor.api.RTProxyImpl
import com.onegravity.rteditor.effects.Effects.ALL_EFFECTS
import kotlinx.android.synthetic.main.activity_editor.*
import ru.noties.markwon.Markwon
import synknotecom.paddi.synknote.Files.Document


class MarkdownEditor : AppCompatActivity() {

    private var editedSinceLastSave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkThemeSettingsCheckbox", false))
            setTheme(R.style.AppTheme_Dark_General)
        else
            setTheme(R.style.AppTheme_General)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Back button
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.elevation = 0f
        }

        // Create RTManager
        val rtApi = RTApi(this, RTProxyImpl(this), RTMediaFactoryImpl(this, true))
        val rtManager = RTManager(rtApi, savedInstanceState)
        rtManager.registerEditor(markdown_editor, true)

        // Fill document
        title = intent.getStringExtra("title")
        markdown_editor.setText(intent.getStringExtra("content"))

        markdown_editor.setOnClickListener {
            if (markdown_editor.hasFocus())
                showSoftwareKeyboard(true)
        }

        setOnFocusChangeListener()
        addTextChangedListener()
        autoSave() // Start the auto-save loop

        // Layout changed, highlight markdown
         var justCreated = true
        markdown_editor.addOnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            if (justCreated) {
                layoutChanged()
                justCreated = false
            }
        }

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

    private fun autoSave() {
        val handler = Handler()
        val delay = 5000L //milliseconds

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (editedSinceLastSave) {
                    Document().save(applicationContext, intent.getStringExtra("filename"), markdown_editor)
                    editedSinceLastSave = false
                }

                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    private fun addTextChangedListener() {
        markdown_editor.addTextChangedListener(object:TextWatcher {
            var linePointBeforeChange: Array<Int> = emptyArray()
            var lastCharSequenceLength = 0
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {
                linePointBeforeChange = getCurrentLinePosition(markdown_editor)
                lastCharSequenceLength = p0!!.count()
            }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                editedSinceLastSave = true

                val currentLinePoint = getCurrentLinePosition(markdown_editor)
                if (p0!!.count() == 1 || markdown_editor.text.length >= currentLinePoint[1]) {
                    styleMarkdown(currentLinePoint[0], currentLinePoint[1])
                } else if (p0.count() > 1 && lastCharSequenceLength < p0.count()) {
                    styleMarkdown(linePointBeforeChange[0], start + count)
                }
            }
        })
    }

    private fun setOnFocusChangeListener() {
        text_editor.setOnFocusChangeListener { view: View, _: Boolean ->
            if (text_editor.hasFocus()) {
                Markwon.setMarkdown(text_editor, markdown_editor.text.toString())
                showSoftwareKeyboard(false, view)
            }
        }
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

    private fun layoutChanged() {
        // Highlight Markdown
        if (markdown_editor.text.isNotEmpty()) {
            val layout = markdown_editor.layout
            for ((index, _) in markdown_editor.text.lines().withIndex()) {
                val lineStart = layout.getLineStart(index)
                val lineEnd = layout.getLineEnd(index)
                styleMarkdown(lineStart, lineEnd)
            }
        }
    }

    fun styleMarkdown(start: Int, end: Int) {
        var markdown = ""

        if (start in 0..(end - 1))
            markdown = markdown_editor.text.substring(start, end)

        val lexData = Lexer().lex(markdown) // Current line
        for ((typeIndex, modifier) in lexData.withIndex()) {
            var i = 0
            while (i + 1 < modifier.count()) {
                val index1 = start + modifier[i] + getType(typeIndex).length
                val index2 = start + modifier[i + 1]
                makeSelection(index1, index2, typeIndex, true)

                i += 2
            }

            if (modifier.count() % 2 != 0)
                makeSelection(start, end, typeIndex,false)
        }


        // Headings
        var typeIndex = 0
        when {
            markdown.startsWith("# ")   -> typeIndex = 4
            markdown.startsWith("## ")  -> typeIndex = 5
            markdown.startsWith("### ") -> typeIndex = 6
        }

        if (typeIndex >= 4)
            makeSelection(start, end, typeIndex, true)
        else
            makeSelection(start, end, 6, false)
    }

    private fun makeSelection(index1: Int, index2: Int, typeIndex: Int, hasStyle: Boolean) {
        val cursorPosition = markdown_editor.selectionEnd
        val effectIndex    = typeIndexToEffectIndex(typeIndex)
        val effectValues:  List<*> = getEffectValues(typeIndex)
        val hasStyleInt    = hasStyle.toInt().binaryInvert()
        var textColor      = Color.BLACK

        if (getDefaultPref(this).getBoolean("darkThemeSettingsCheckbox", false))
            textColor = Color.WHITE

        // Set style to text inside modifiers
        markdown_editor.setSelection(index1, index2)
        ALL_EFFECTS[effectIndex].applyToSelection(markdown_editor, effectValues[hasStyleInt])

        // Gray out modifiers
        val typeLength = getType(typeIndex).length
        if (effectIndex < 4 && hasStyle) { // Normal modifiers
            markdown_editor.setSelection(index1 - typeLength, index2 + typeLength)          // Select entire thing
            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.parseColor("#A6D3D3D3"))  // Color it
            markdown_editor.setSelection(index1, index2)                                                // Select inside (without modifiers)
        } else if (hasStyle) { // Headings
            markdown_editor.setSelection(index1, index1 + typeLength)                             // Select pound signs
            ALL_EFFECTS[7].applyToSelection(markdown_editor, Color.parseColor("#A6D3D3D3"))  // Color them
            markdown_editor.setSelection(index1 + typeLength)                                           // Select after pound signs
        }

        ALL_EFFECTS[7].applyToSelection(markdown_editor, textColor) // Set to normal color

        // Reset surroundings
            markdown_editor.setSelection(cursorPosition)
            ALL_EFFECTS[effectIndex].applyToSelection(markdown_editor, effectValues[1])
            ALL_EFFECTS[7].applyToSelection(markdown_editor, textColor)
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
            4 -> listOf(90, 42) as T
            5 -> listOf(75, 54) as T
            6 -> listOf(65, 54) as T
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
        Document().save(applicationContext, intent.getStringExtra("filename"), markdown_editor)
    }

}
