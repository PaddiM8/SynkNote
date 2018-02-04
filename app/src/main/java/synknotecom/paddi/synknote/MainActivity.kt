package synknotecom.paddi.synknote

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
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
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.password_dialog.*
import kotlinx.android.synthetic.main.rename_dialog.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory

@Suppress("JAVA_CLASS_ON_COMPANION")
class MainActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    object FileList {
        var files = arrayListOf<File>()
        lateinit var adapter: Adapter
    }

    object Protection {
        var askForPassword = true
        var encryptionKey = ""
        val salt = "salt" // TODO: Fix salt
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val defaultPref = PreferenceManager.getDefaultSharedPreferences(this)
        val pref = applicationContext.getSharedPreferences("DataPref", MODE_PRIVATE)

        if (defaultPref.getBoolean("darkThemeSettingsCheckbox", false))
            setTheme(R.style.AppTheme_Dark)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        val passwordLockSettingEnabled = defaultPref.getBoolean("passwordLockSwitch", false)

        if (passwordLockSettingEnabled && Protection.askForPassword && pref.getString("password_hash", null) != null) {
            val passwordDialog = createPasswordDialog()
            passwordDialog.show()
            Protection.askForPassword = false
            toggleKeyboard()

            passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val dialogInput = passwordDialog.password_input.text.toString()
                if (BCrypt.checkpw(dialogInput, pref.getString("password_hash", null))) {
                    passwordDialog.password_input.setText("")
                    passwordDialog.dismiss()
                    loadFileList()
                    loadDocuments()
                    toggleKeyboard()

                    Protection.encryptionKey = PBKDF2Algo.generateHash(dialogInput, Protection.salt.toByteArray())
                }
            }

        } else {
            loadFileList()
            loadDocuments()
        }

        setSupportActionBar(toolbar)
        fab.setImageResource(R.drawable.ic_add)

        initSwipe()

        val newDocumentDialog = createNewDocumentDialog()

        fab.setOnClickListener {
            newDocumentDialog.show()
            toggleKeyboard()

            newDocumentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                createDocument(newDocumentDialog.document_name_input.text.toString(),
                        newDocumentDialog.spinner.selectedItem.toString(),
                        window.decorView)
                newDocumentDialog.document_name_input.text.clear()
                newDocumentDialog.dismiss()
            })

            newDocumentDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
                newDocumentDialog.dismiss()
                toggleKeyboard()
            })
        }

        documentsListView.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
            openDocument(i, window.decorView)
        }
    }

    private fun initSwipe() {
        val renameDialog = createRenameDialog()
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemId = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    deleteDocument(itemId)
                } else {
                    renameDialog.show()
                    toggleKeyboard()

                    val view = viewHolder.itemView
                    if (view.parent != null)
                        (view.parent as ViewGroup).removeView(view)

                    renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                        renameDocument(itemId, renameDialog.rename_document_input.text.toString(), window.decorView)
                        loadDocuments() // TODO: Uh, make more efficient
                        renameDialog.rename_document_input.text.clear()
                        renameDialog.dismiss()
                        toggleKeyboard()
                    })

                    renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
                        renameDialog.dismiss()
                        toggleKeyboard()
                    })
                }
            }

            override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3
                    var paint = Paint()
                    val itemViewTop    : Float = itemView.top.toFloat()
                    val itemViewLeft   : Float = itemView.left.toFloat()
                    val itemViewBottom : Float = itemView.bottom.toFloat()
                    val itemViewRight  : Float = itemView.right.toFloat()

                    if (dX > 0) {
                        paint.color = Color.parseColor("#388E3C")
                        val background = RectF(itemViewLeft, itemViewTop, dX, itemViewBottom)
                        canvas.drawRect(background, paint)

                        val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_edit_white)
                        canvas.drawBitmap(icon, null, createIconRectangle("left", itemView, width), paint)
                    } else {
                        paint.color = Color.parseColor("#D32F2F")
                        val background = RectF(itemViewRight, itemViewTop,  dX, itemViewBottom)
                        canvas.drawRect(background, paint)

                        val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete_white)
                        canvas.drawBitmap(icon, null, createIconRectangle("right", itemView, width), paint)
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView)
    }

    private fun createIconRectangle(direction: String, itemView: View, width: Float) : RectF {
        return if (direction == "left")
            RectF(
                       itemView.left.toFloat() + width,
                       itemView.top.toFloat() + width,
                      itemView.left.toFloat() + 2 * width,
                    itemView.bottom.toFloat() - width
            )
        else
            RectF(
                       itemView.right.toFloat() + width,
                       itemView.top.toFloat() + width,
                      itemView.right.toFloat() + 2 * width,
                    itemView.bottom.toFloat() - width
            )
    }

    override fun onResume() {
        super.onResume()
        loadFileList()
    }

    override fun onPause() {
        super.onPause()
        Protection.askForPassword = true
    }

    private fun toggleKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun showSoftwareKeyboard(show: Boolean, view: View = View(this)) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (show)
            imm.showSoftInput(markdownEditor, 0)
        else
            imm.hideSoftInputFromWindow(view.windowToken, 0)
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
        FileList.adapter = Adapter(FileList.files)
        recyclerView.adapter = FileList.adapter
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

    private fun createRenameDialog() : AlertDialog {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.rename_dialog, null)
        dialogView.findViewById<EditText>(R.id.rename_document_input)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        dialog.setPositiveButton("Rename", { _: DialogInterface, _: Int -> })
        dialog.setNegativeButton("Cancel", { _: DialogInterface, _: Int -> })

        return dialog.create()
    }

    private fun createPasswordDialog() : AlertDialog {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.password_dialog, null)
        dialogView.findViewById<EditText>(R.id.password_input)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        dialog.setPositiveButton("Enter", { _: DialogInterface, _: Int -> })

        return dialog.create()
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

        if (item.itemId == R.id.action_settings) {
            Protection.askForPassword = false
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }

        return true
    }
}
