package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.onegravity.rteditor.effects.Effect
import com.onegravity.rteditor.effects.Effects
import kotlinx.android.synthetic.main.activity_editor.*
import ru.noties.markwon.Markwon
import java.lang.Math.floor
import com.onegravity.rteditor.RTManager
import com.onegravity.rteditor.api.RTMediaFactoryImpl
import com.onegravity.rteditor.api.RTProxyImpl
import com.onegravity.rteditor.api.RTApi
import org.apache.commons.lang.StringUtils


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
            override fun afterTextChanged(p0: Editable?) {

            }

            var lastCount = 0
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            var lastCounts = arrayOf(0, 0, 0, 0)
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val types = arrayOf("<br>", "<u>", "<i>", "<i>")
                val styleSpans: Array<Effect<Boolean, *>> = arrayOf(
                        Effects.BOLD,
                        Effects.UNDERLINE,
                        Effects.ITALIC,
                        Effects.ITALIC
                )

                val inputCopy = p0.toString().replace("**", "<br>")
                                             .replace("__", "<u>")
                                             .replace("*", "<i>")
                                             .replace("_", "<i>")
                var spannableString = SpannableStringBuilder(markdown_editor.text.toString())
                val selection = markdown_editor.selectionStart
                //val occurrences = getOccurrenceAmount(inputCopy, types)

                for ((index, type) in types.withIndex()) {
                    val occurrences = getOccurrenceAmount(inputCopy, type)
                    Log.d("occurrences", occurrences.toString() + ", " + inputCopy)
                    if (occurrences != lastCounts[index]) {
                        lastCounts[index] = occurrences
                        spannableString = findMarkdown(spannableString, inputCopy, type, styleSpans[index])
                    }
                }

                /*if (occurrences != lastCount) {
                    markdown_editor.text = spannableString
                    markdown_editor.setSelection(selection)
                }*/
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

    private fun findMarkdown(original: SpannableStringBuilder, copy: String, type: String, effect: Effect<*, *>): SpannableStringBuilder {
        val output = SpannableStringBuilder(original)
        val occurrenceAmount = getOccurrenceAmount(copy, type)

        var i = 1
        while (i < occurrenceAmount && occurrenceAmount % 2 == 0) {
            val actualTypeLength = getActualType(type).length
            val index1 = StringUtils.ordinalIndexOf(copy, type, i) - (i - 1)
            val index2 = StringUtils.ordinalIndexOf(copy, type, i + 1) - type.length //copy.indexOf(type, index1) - actualTypeLength
            val lastCursorPosition = markdown_editor.selectionStart
            Log.d("Yes", StringUtils.ordinalIndexOf(copy, type, i).toString() + ", " + i.toString())
            markdown_editor.setSelection(index1, index2)
            markdown_editor.applyEffect(Effects.BOLD, true)
            markdown_editor.setSelection(index2)
            markdown_editor.applyEffect(Effects.BOLD, false)
            markdown_editor.setSelection(lastCursorPosition)
            i += 2
        }

        return output
    }

    private fun getActualType(type: String): String {
        return when (type) {
            "<br>" -> "**"
            "<u>" -> "__"
            "<i>" -> "_"
            else -> type
        }
    }

    private fun getOccurrenceAmount(input: String, match: String): Int {
        return input.split(match).count() - 1
    }

    private fun getOccurrenceAmount(input: String, matches: Array<String>): Int {
        var totalValue = 0
        for (match in matches)
            totalValue += input.split(match).count() - 1

        return totalValue
    }

    private fun styleString(input: SpannableStringBuilder, start: Int, end: Int, styleSpan: Any): SpannableStringBuilder {
        input.setSpan(styleSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return input
    }

    /*private fun styleString(input: SpannableStringBuilder, findIn: String, selectionPoint: Point,
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
    }*/

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
