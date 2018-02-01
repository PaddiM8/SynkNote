package synknotecom.paddi.synknote

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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

    private var itemList = arrayListOf<Document>()
    private lateinit var adapter: Adapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        setSupportActionBar(toolbar)
        fab.setImageResource(R.drawable.ic_add)

        adapter = Adapter(itemList)
        recyclerView.adapter = adapter

        loadFiles()

        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.new_document_dialog, null)
        dialogView.findViewById<EditText>(R.id.document_name_input)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        dialog.setPositiveButton("Create", { _: DialogInterface, i: Int -> })
        dialog.setNegativeButton("Cancel", { _: DialogInterface, i: Int -> })

        val customDialog = dialog.create()

        fab.setOnClickListener {
            customDialog.show()
            toggleKeyboard()

            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                createDocument(customDialog.document_name_input.text.toString(), customDialog.spinner.selectedItem.toString())
                customDialog.document_name_input.text.clear()
                customDialog.dismiss()
            })

            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
                customDialog.dismiss()
            })
        }

        documentsListView.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            openDocument(i)
        }
    }

    override fun onResume() {
        super.onResume()
        loadFiles()
    }

    private fun toggleKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun createDocument(name: String, type: String) {
        val fileName = name + getFileExtensionFromType(type)
        itemList.add(Document(name, "02/02", fileName))
        adapter.notifyDataSetChanged()

        val fos = openFileOutput(fileName, Context.MODE_PRIVATE)
        fos.write("".toByteArray()) // Create empty file
        fos.close()

        //updateList()
        openDocument(itemList.count() - 1)
    }

    private fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        var params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(left, top, right, bottom)
        view.layoutParams = params
    }

    private fun loadFiles() {
        var directory = File(applicationInfo.dataDir + "/files/") // Directory from application root

        if (directory.exists()) {
            var files = directory.listFiles()
            itemList.clear()

            Arrays.sort(files) { a, b -> java.lang.Long.compare(b.lastModified(), a.lastModified()) }

            for (file in files) {
                itemList.add(Document(file.nameWithoutExtension, getDate(file.lastModified(), "dd/MM"), file.name))
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun openDocument(id: Int) {
        var directory = File(applicationInfo.dataDir + "/files/") // Directory from application root
        val documentFile = directory.listFiles()[id]
        var intent = Intent(this, Editor::class.java)

        if (documentFile.extension == "txt")
            intent = Intent(this, NormalEditor::class.java)

        intent.putExtra("title", documentFile.nameWithoutExtension)
        intent.putExtra("content", documentFile.readText())
        intent.putExtra("filename", documentFile.name)
        startActivity(intent)
    }

    private fun updateList() {
        //documentsListView.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemList)
        //adapter = Adapter(itemList)
        //recyclerView.adapter = adapter
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
