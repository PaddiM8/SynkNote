package synknotecom.paddi.synknote

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main_window.*
import kotlinx.android.synthetic.main.new_document_dialog.*
import java.io.File
import java.util.*
import kotlinx.android.synthetic.main.password_dialog.*
import java.io.OutputStreamWriter
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.view.KeyEvent.KEYCODE_BACK




@Suppress("JAVA_CLASS_ON_COMPANION")
class MainActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    object FileList {
        var files = arrayListOf<File>()
        var currentDirectory: String = ""
        lateinit var adapter: Adapter
    }

    object Protection {
        var askForPassword = true
        var encryptionKey = ""
        var salt = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        loadTheme(this)

        // Initialization
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)
        linearLayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = linearLayoutManager

        setSupportActionBar(toolbar)
        fab.setImageResource(R.drawable.ic_add)
        Protection.salt = getSalt()

        promptPermissions()

        // Make sure localFolderPath is correct
        val localFolderTextInput = getDefaultPref(this).getString("localFolderEditText", null)
        if (localFolderTextInput == null)
            getDefaultPref(this).edit()
                    .putString("localFolderEditText", applicationInfo.dataDir + "/files/").apply()
        else if (!localFolderTextInput.endsWith("/"))
            getDefaultPref(this).edit()
                .putString("localFolderEditText", localFolderTextInput + "/").apply()

        // Load currentDirectory
        if (FileList.currentDirectory == "")
            FileList.currentDirectory = getDefaultPref(this)
                    .getString("localFolderEditText", null)

        // Password Lock
        val passwordLockSettingEnabled = getDefaultPref(this)
                .getBoolean("passwordLockSwitch", false)
        val passwordHash = getPref("DataPref", this)
                .getString("password_hash", null)

        if (passwordLockSettingEnabled && Protection.askForPassword && passwordHash != null) {
            askForPassword()
        } else {
            // Set to default encryption key, if the user choose to not use password lock, the encryption key won't be protected locally.
            if (!passwordLockSettingEnabled)
                Protection.encryptionKey = getEncryptionKey()
            loadFileList()
            loadDocuments()
        }

        // Back button
        if (isInMainDirectory(this))
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        else
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup ItemTouchHelper
        val helper = ItemTouchHelper(initializeItemTouchHelper(window.decorView))
        helper.attachToRecyclerView(recycler_view)

        // Fab click event
        fab.setOnClickListener { fabOnClick() }
    }

    override fun onResume() {
        super.onResume()
        loadFileList()
    }

    override fun onPause() {
        super.onPause()
        Protection.askForPassword = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack()
            true
        } else
            super.onKeyDown(keyCode, event)
    }

    private fun onBack() {
        // Go to main directory
        if (!isInMainDirectory(this)) {
            FileList.currentDirectory = getSaveLocation(this)
            loadFileList()
            loadDocuments()
        }
    }

    private fun promptPermissions() {
        // Storage Permission not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    }

    private fun toggleKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun getEncryptionKey(): String {
        return if (getPref("Security", this).getString("defaultEncryptionKey", null) == null) {
            val key = generateRandomEncryptionKey()
            getPref("Security", this).edit()
                    .putString("defaultEncryptionKey", key)
                    .apply()
            key
        } else {
            getPref("Security", this).getString("defaultEncryptionKey", null)
        }
    }

    private fun getSalt(): String {
        return if (getPref("Security", this).getString("salt", null) == null) {
            val salt = BCrypt.gensalt()
            getPref("Security", this).edit()
                    .putString("salt", salt)
                    .apply()
            salt
        } else {
            getPref("Security", this).getString("salt", null)
        }
    }

    private fun askForPassword() {
        val passwordDialog = createPasswordDialog(this)
        passwordDialog.show()
        Protection.askForPassword = false
        toggleKeyboard()

        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val pref = applicationContext.getSharedPreferences("DataPref", MODE_PRIVATE)
            val dialogInput = passwordDialog.password_input.text.toString()

            if (PBKDF2Algo.generateHash(dialogInput, Protection.salt.toByteArray()) == pref.getString("password_hash", null)) {
                passwordDialog.password_input.setText("")
                passwordDialog.dismiss()
                loadFileList()
                loadDocuments()
                toggleKeyboard()

                Protection.encryptionKey = PBKDF2Algo.generateHash(dialogInput, Protection.salt.toByteArray())
            }
        }
    }

    private fun fabOnClick() {
        val newDocumentDialog = createNewDocumentDialog(this)
        newDocumentDialog.show()
        showSoftwareKeyboard(true, newDocumentDialog.document_name_input)

        // New document dialog on buttonCreate click
        newDocumentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
            val documentType = newDocumentDialog.spinner.selectedItem.toString()
            val documentName = newDocumentDialog.document_name_input.text.toString()

            if (documentType == "Folder")
                createFolder(documentName)
            else
                createDocument(documentName, documentType, window.decorView)

            newDocumentDialog.document_name_input.text.clear()
            newDocumentDialog.dismiss()
        })

        newDocumentDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
            newDocumentDialog.dismiss()
            toggleKeyboard()
        })
    }

    fun loadDocuments() {
        FileList.adapter = Adapter(FileList.files, this)
        recycler_view.adapter = FileList.adapter
    }

    fun loadFileList() {
        val directory = File(MainActivity.FileList.currentDirectory) // Directory from application root

        if (directory.exists()) {
            val files = directory.listFiles()

            if (files != null) {
                Arrays.sort(files) { a, b -> java.lang.Long.compare(b.lastModified(), a.lastModified()) }
                MainActivity.FileList.files = ArrayList(files.toList())
            }
        }
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
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            Protection.askForPassword = false
        }

        return true
    }
}
