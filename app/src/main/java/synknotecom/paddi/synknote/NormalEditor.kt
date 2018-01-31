package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.activity_normal_editor.*

class NormalEditor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_editor)

        var actionBar = supportActionBar
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true)

        title = intent.getStringExtra("title")
        normalTextEditor.setText(intent.getStringExtra("content"))


        normalTextEditor.setOnClickListener {
            if (normalTextEditor.hasFocus())
                showSoftwareKeyboard(true)
        }
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

    override fun onPause() {
        super.onPause()
        saveDocument(applicationContext, intent.getStringExtra("filename"), normalTextEditor)
        Toast.makeText(this,"Document saved.", Toast.LENGTH_SHORT).show()
    }

    private fun showSoftwareKeyboard(show: Boolean, view: View = View(this)) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (show)
            imm.showSoftInput(normalTextEditor, 0)
        else
            imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
