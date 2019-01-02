package org.synknote

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main_window.*
import kotlinx.android.synthetic.main.new_document_dialog.*
import java.io.File
import java.util.*
import kotlinx.android.synthetic.main.password_dialog.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import org.synknote.adapters.Adapter
import org.synknote.algorithms.BCrypt
import org.synknote.algorithms.PBKDF2Algo
import org.synknote.files.Document
import org.synknote.files.Folder
import org.synknote.misc.*
import android.support.v4.view.GravityCompat
import android.text.format.DateUtils
import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.activity_register.view.*
import kotlinx.android.synthetic.main.add_notebook_dialog.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import org.synknote.R.id.*
import org.synknote.files.Notebook
import org.synknote.files.NotebookData
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager
import kotlin.reflect.jvm.internal.impl.serialization.ProtoBuf


@Suppress("JAVA_CLASS_ON_COMPANION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var linearLayoutManager: LinearLayoutManager

    object FileList {
        var files = arrayListOf<File>()
        var currentDirectory: String = ""
        var currentNotebook: NotebookData = NotebookData("", "", "",false)
        lateinit var adapter: Adapter
    }

    object Protection {
        var askForPassword = true
        var encryptionKey = ""
        var salt = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager(this, ActivityTypes.MAIN).loadTheme()

        // Initialization
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)
        navigation_view.setNavigationItemSelectedListener(this)
        linearLayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = linearLayoutManager

        promptPermissions()

        // Create default notebook if it doesn't exist
        val notebooksPref = PrefManager(this, PrefGroup.Notebooks).get()
        if (notebooksPref.all.count() == 0) {
            val defaultNotebookName = "Notebook1"
            val notebook = Notebook(this, defaultNotebookName)
            notebook.add(
                    navigation_view.menu,
                    fixUrl(application.filesDir.absolutePath) + defaultNotebookName,
                    PrefManager(this, PrefGroup.Sync).contains("userId") // If userId is set, enable sync on default notebook
            )
        }

        toolbar.title = PrefManager(this, PrefGroup.NotebooksData)
                .getString("last_notebook")
        setSupportActionBar(toolbar)
        fab.setImageResource(R.drawable.ic_add)
        Protection.salt = getSalt()

        // Select last used notebook
        if (FileList.currentNotebook.name == "") {
            val lastNotebookName = PrefManager(this, PrefGroup.NotebooksData)
                    .getString("last_notebook")
            Notebook(this, lastNotebookName).select()
            toolbar.title = lastNotebookName
        }

        // Password Lock
        val passwordLockSettingEnabled = getDefaultPref(this)
                .getBoolean("password_lock_switch", false)
        val passwordHash = PrefManager(this, PrefGroup.Security)
                .getString("password_hash")

        if (passwordLockSettingEnabled && Protection.askForPassword && !passwordHash.isEmpty()) {
            askForPassword()
        } else {
            // Set to default encryption key, if the user choose to not use password lock, the encryption key won't be protected locally.
            if (!passwordLockSettingEnabled) {
                Protection.encryptionKey = getEncryptionKey()
            }
            //loadFileList()
            loadDocuments()
        }

        setUpDrawerToggle()

        // Update login status
        if (PrefManager(this, PrefGroup.Sync).getBoolean("logged_in")) {
            val header = navigation_view.getHeaderView(0)
            header.logged_in_text.text = getString(R.string.loggedIn)
            header.login_button.visibility    = View.GONE
            header.register_button.visibility = View.GONE
            header.logout_button.visibility   = View.VISIBLE
        }

        // Fab click event
        fab.setOnClickListener { fabOnClick() }

        // Register button click
        navigation_view.getHeaderView(0).register_button.setOnClickListener {
            val registerActivity = Intent(this, RegisterActivity::class.java)
            startActivity(registerActivity)
        }

        // Login button click
        navigation_view.getHeaderView(0).login_button.setOnClickListener {
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }

        // Logout button click
        navigation_view.getHeaderView(0).logout_button.setOnClickListener {
            PrefManager(this, PrefGroup.Sync)
                    .setString("userId", "")
                    .setString("token", "")
                    .setBoolean("logged_in", false)

            val notebooks = PrefManager(this, PrefGroup.Notebooks)
            for (notebookJson in notebooks.get().all.entries) {
                val notebookMap = GsonBuilder().create().fromJson(notebookJson.value.toString(),
                                                                  Map::class.java)
                val name = notebookMap["name"].toString()
                val location = notebookMap["location"].toString()

                if (notebookMap["sync"].toString() == "true" &&
                    location.startsWith(application.filesDir.canonicalPath)) {
                    File(fixUrl(location) + name).deleteRecursively()
                    notebooks.get().edit().remove(name).apply()
                }
            }

            val intent = intent
            finish()
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        loadFileList()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Navigation item clicks
        when (item.itemId) {
            add_notebook_item -> {
                showNoteBookDialog()
            }

            edit_notebook_item -> {

            }
        }

        // Open notebook
        if (item.groupId == notebooks_group) { // Notebooks group
            Notebook(this, item.title.toString()).select()
            toolbar.title = item.title.toString()
            loadDocuments()
            drawer.closeDrawer(GravityCompat.START)
        }

        return true
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
            FileList.currentDirectory = File(FileList.currentDirectory).parentFile.absolutePath
            //loadFileList()
            loadDocuments()
        }
    }

    private fun promptPermissions() {
        // Storage Permission not granted
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun toggleKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun getEncryptionKey(): String {
        return if (!PrefManager(this, PrefGroup.Security).contains("default_encryption_key")) {
            val key = generateRandomEncryptionKey()
            PrefManager(this, PrefGroup.Security)
                    .setString("default_encryption_key", key)
            key
        } else {
            PrefManager(this, PrefGroup.Security).getString("default_encryption_key")
        }
    }

    private fun getSalt(): String {
        return if (!PrefManager(this, PrefGroup.Security).contains("salt")) {
            val salt = BCrypt.gensalt()
            PrefManager(this, PrefGroup.Security)
                    .setString("salt", salt)
            salt
        } else {
            PrefManager(this, PrefGroup.Security).getString("salt")
        }
    }

    private fun askForPassword() {
        val passwordDialog = createPasswordDialog(this)
        passwordDialog.show()
        Protection.askForPassword = false
        toggleKeyboard()

        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val pref = applicationContext.getSharedPreferences("Security", MODE_PRIVATE)
            val dialogInput = passwordDialog.password_input.text.toString()

            if (dialogInput.isNotEmpty()) {
                if (PBKDF2Algo.generateHash(dialogInput, Protection.salt.toByteArray()) == pref.getString("password_hash", null)) {
                    passwordDialog.password_input.setText("")
                    passwordDialog.dismiss()
                    //loadFileList()
                    loadDocuments()
                    toggleKeyboard()

                    Protection.encryptionKey = PBKDF2Algo.generateHash(dialogInput, Protection.salt.toByteArray())
                }
            }
        }
    }

    private fun showNoteBookDialog(){
        val dialog = newNoteBookDialog(this)
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val notebookName     = dialog.notebook_name_input.text.toString()
            var notebookLocation = dialog.notebook_location_input.text.toString()
            val syncNotebook     = dialog.notebook_sync_input.isChecked

            if (notebookLocation == "")
                notebookLocation = fixUrl(application.filesDir.absolutePath) + notebookName

            Notebook(this, notebookName).add(
                    navigation_view.menu,
                    notebookLocation,
                    syncNotebook
            )

            dialog.dismiss()
            drawer.openDrawer(Gravity.START)
        }
    }

    private fun loadNotebooks() {
        val allEntries = PrefManager(this, PrefGroup.Notebooks).get().all.keys
        val icon = ThemeManager(this).getIcon(Icons.NOTEBOOK)
        val subMenu = navigation_view.menu.findItem(R.id.notebooks_item).subMenu
        subMenu.clear()

        for (entry in allEntries.sortedDescending()) {
            subMenu.add(
                    notebooks_group,
                    100 + navigation_view.menu.size(),
                    Menu.NONE,
                    entry
            ).icon = ContextCompat.getDrawable(this, icon)

        }

        subMenu.setGroupCheckable(notebooks_group, true, true)
    }

    private fun fabOnClick() {
        val newDocumentDialog = createNewDocumentDialog(this)
        newDocumentDialog.show()
        showSoftwareKeyboard(true, newDocumentDialog.document_name_input)

        // New document dialog on buttonCreate click
        newDocumentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val documentType = newDocumentDialog.spinner.selectedItem.toString()
            val documentName = newDocumentDialog.document_name_input.text.toString()


            if (documentType == "Folder")
                Folder().create(documentName)
            else
                Document().create(documentName, documentType, window.decorView, FileList.currentNotebook.sync)

            newDocumentDialog.document_name_input.text.clear()
            newDocumentDialog.dismiss()
        }

        newDocumentDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            newDocumentDialog.dismiss()
            toggleKeyboard()
        }
    }

    fun loadDocuments() {
        loadNotebooks()
        loadFileList()
        FileList.adapter = Adapter(FileList.files, this)
        recycler_view.adapter = FileList.adapter
    }

    private fun loadFileList() {
        val directory = File(FileList.currentDirectory) // Directory from application root
        //supportActionBar?.setDisplayHomeAsUpEnabled(!isInMainDirectory(this))

        if (directory.exists()) {
            val allFiles = directory.listFiles() ?: return
            val pinned   = allFiles.filter { it.name.startsWith(".") }
            val folders  = allFiles.filter { it.isDirectory && !it.name.startsWith(".") }
            val files    = allFiles.filter { it.isFile && !it.name.startsWith(".") }

            folders.sortedWith(compareBy({ it.lastModified() }, { it.lastModified() }))
            files.sortedWith  (compareBy({ it.lastModified() }, { it.lastModified() }))
            FileList.files = ArrayList(pinned + folders + files)
        }
    }

    private fun setUpDrawerToggle() {
        val mDrawerToggle = ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )

        drawer.post { mDrawerToggle.syncState() }
        drawer.setDrawerListener(mDrawerToggle)
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
        } else if (item.itemId == android.R.id.home)
            onBack()

        return true
    }
}
