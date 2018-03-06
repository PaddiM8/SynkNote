package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
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
        markdown_editor.setText(intent.getStringExtra("content"))

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

        /*text_editor.setOnKeyListener { view: View, i: Int, keyEvent: KeyEvent ->
            val linePoint = getCurrentLinePoint(text_editor)
            val lineText = text_editor.text.toString().substring(linePoint.x, linePoint.y)
            val italicMatches = Regex("([*_])(?:(?=(\\\\?))\\2.)*?\\1")
                    .findAll(lineText)

            Log.d("italicMatches Count", italicMatches.count().toString())
            for (match in italicMatches) {
                Log.d("Match!", "Match!")
                val spannableString = SpannableStringBuilder(text_editor.text.toString())
                spannableString.setSpan(StyleSpan(Typeface.BOLD),
                                        match.range.first,
                                        match.range.last,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                Log.d("spannableString", spannableString.toString())
                text_editor.text = spannableString
            }

            true
        }*/

        var ignoredRunsLeft = 0
        markdown_editor.addTextChangedListener(object:TextWatcher {
            override fun afterTextChanged(s:Editable) {
                if (ignoredRunsLeft == 0) {
                    val linePoint = getCurrentLinePoint(markdown_editor)
                    val lineText = markdown_editor.text.toString().substring(linePoint.x, linePoint.y)
                    val originalString = SpannableStringBuilder(markdown_editor.text.toString())

                    /* Find and mark italic
                    val italicMatches = Regex("([*_])(?:(?=(\\\\?))\\2.)*?\\1")
                            .findAll(lineText)
                    for (match in italicMatches) {
                        spannableString.setSpan(StyleSpan(Typeface.ITALIC),
                                linePoint.x + match.range.first,
                                +linePoint.x + match.range.last,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    // Find and mark bold
                    val boldMatches = Regex("(\\*\\*)(?:(?=(\\\\?))\\2.)*?\\1")
                            .findAll(lineText)
                    for (match in boldMatches) {
                        spannableString.setSpan(StyleSpan(Typeface.BOLD),
                                linePoint.x + match.range.first,
                                +linePoint.x + match.range.last,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    // Find and mark underline
                    val underlineMatches = Regex("(__)(?:(?=(\\\\?))\\2.)*?\\1")
                            .findAll(lineText)
                    for (match in underlineMatches) {
                        spannableString.setSpan(UnderlineSpan(),
                                linePoint.x + match.range.first,
                                +linePoint.x + match.range.last,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }*/

                    val patterns = listOf(
                        "([*_])(?:(?=(\\\\?))\\2.)*?\\1",    // Italic
                        "(\\*\\*)(?:(?=(\\\\?))\\2.)*?\\1",  // Bold
                        "(__)(?:(?=(\\\\?))\\2.)*?\\1"       // Underline
                    )

                    val styleType = listOf(
                            "italic",
                            "bold",
                            "underline"
                    )

                    var spannableString = originalString
                    patterns.forEachIndexed { index, _ ->
                        spannableString = styleString(spannableString, lineText,
                                linePoint, patterns[index], styleType[index])
                    }

                    // Set styling to EditText if the string's styling changed
                    if (spannableString != originalString) {
                        val cursorPosition = markdown_editor.selectionStart
                        ignoredRunsLeft++
                        markdown_editor.text = spannableString
                        markdown_editor.setSelection(cursorPosition)
                    }
                }

                if (ignoredRunsLeft > 0)
                    ignoredRunsLeft--
            }
            override fun beforeTextChanged(s:CharSequence, start:Int,
                                  count:Int, after:Int) {}
            override fun onTextChanged(s:CharSequence, start:Int,
                                       before:Int, count:Int) {
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

    private fun styleString(input: SpannableStringBuilder, findIn: String, selectionPoint: Point,
                            pattern: String, styleType: String): SpannableStringBuilder {
        // Find and mark
        val italicMatches = Regex(pattern)
                .findAll(findIn)
        for (match in italicMatches) {
            // Style selection
            input.setSpan(getStyleSpanFromString(styleType),
                    selectionPoint.x + match.range.first,
                    selectionPoint.x + match.range.last,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Gray out * and _ and such
            if (styleType == "italic" || styleType == "bold" || styleType == "underline") {
                input.setSpan(ForegroundColorSpan(0xd1dd23),
                        selectionPoint.x + match.range.first,
                        selectionPoint.x + match.range.first + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                input.setSpan(ForegroundColorSpan(0xd1dd23),
                        selectionPoint.x + match.range.last,
                        selectionPoint.x + match.range.last + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return input
    }

    private inline fun <reified T> getStyleSpanFromString(style: String): T {
        return when (style) {
            "italic" -> StyleSpan(Typeface.ITALIC) as T
            "bold" -> StyleSpan(Typeface.BOLD) as T
            "underline" -> UnderlineSpan() as T
            else -> StyleSpan(Typeface.NORMAL) as T
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
