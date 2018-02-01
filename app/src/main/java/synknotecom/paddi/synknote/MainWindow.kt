package synknotecom.paddi.synknote

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlinx.android.synthetic.main.activity_main_window.*
import kotlinx.android.synthetic.main.content_main_window.*
import kotlinx.android.synthetic.main.new_document_dialog.*
import java.io.File
import java.util.*

class MainWindow : AppCompatActivity() {

    //private lateinit var adapter: Adapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    object FileList {
        var files = arrayListOf<File>()
        lateinit var adapter: Adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        setSupportActionBar(toolbar)
        fab.setImageResource(R.drawable.ic_add)

        loadDocuments()

        val customDialog = createNewDocumentDialog()

        fab.setOnClickListener {
            customDialog.show()
            toggleKeyboard()

            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                createDocument(customDialog.document_name_input.text.toString(),
                               customDialog.spinner.selectedItem.toString(),
                               window.decorView)
                customDialog.document_name_input.text.clear()
                customDialog.dismiss()
            })

            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
                customDialog.dismiss()
            })
        }

        documentsListView.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
            openDocument(i, window.decorView)
        }
    }

    override fun onResume() {
        super.onResume()
        loadFileList()
    }

    private fun toggleKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    @SuppressLint("InflateParams")
    private fun createNewDocumentDialog() : AlertDialog {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.new_document_dialog, null)
        dialogView.findViewById<EditText>(R.id.document_name_input)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        dialog.setPositiveButton("Create", { _: DialogInterface, _: Int -> })
        dialog.setNegativeButton("Cancel", { _: DialogInterface, _: Int -> })

        return dialog.create()
    }

    private fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(left, top, right, bottom)
        view.layoutParams = params
    }

    private fun loadFileList() {
        val directory = File(applicationInfo.dataDir + "/files/") // Directory from application root

        if (directory.exists()) {
            val files = directory.listFiles()
            Arrays.sort(files) { a, b -> java.lang.Long.compare(b.lastModified(), a.lastModified()) }
            FileList.files = ArrayList(files.toList())
        }
    }

    private fun loadDocuments() {
        loadFileList()
        FileList.adapter = Adapter(FileList.files)
        recyclerView.adapter = FileList.adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main_window, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
