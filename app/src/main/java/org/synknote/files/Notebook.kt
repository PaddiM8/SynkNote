package org.synknote.files

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import com.google.gson.GsonBuilder
import org.synknote.MainActivity
import org.synknote.R
import org.synknote.ThemeManager
import org.synknote.misc.Icons
import org.synknote.models.NoteSkeleton
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager
import org.synknote.sync.SyncManager
import java.io.File

class Notebook(context: Context, name: String) {
    private val _context = context
    private val _name = name

    fun add(menu: Menu, location: String, sync: Boolean, notebookId: String = ""): Notebook {
        add(location, sync, notebookId)

        val icon = ThemeManager(_context).getIcon(Icons.NOTEBOOK)
        val subMenu = menu.findItem(R.id.notebooks_item).subMenu
        subMenu.setGroupCheckable(R.id.notebooks_group, true, true)
        subMenu.add(
                R.id.notebooks_group,
                100 + menu.size(),
                Menu.NONE,
                _name
        ).icon = ContextCompat.getDrawable(_context, icon)
        select()

        return this
    }

    fun add(location: String, sync: Boolean, notebookId: String = "", addRemotely: Boolean = sync): Notebook {

        val notebookData = NotebookData(notebookId, _name, location, sync)

        if (sync && addRemotely) {
            val syncPref = PrefManager(_context, PrefGroup.Sync)
            val result = SyncManager(_context).createNotebook(
                    syncPref.getString("userId"),
                    syncPref.getString("token"),
                    _name
            )

            syncPref.setString("token", result["token"].toString())
            notebookData.id = result["notebookId"].toString()
        }

        val gson = GsonBuilder().create()
        PrefManager(_context, PrefGroup.Notebooks).setString(_name, gson.toJson(notebookData)) // Convert NotebookData object to json and put in pref
        File(location).mkdirs()

        return this
    }

    fun select(): NotebookData {
        //val notebookLocation = getPref("notebooks", _context)
        //        .getString(_name, null)
        //getPref("notebooksData", _context).edit()
        //        .putString("last_notebook", _name).apply()
        //val notebookLocation = PrefManager(_context, PrefGroup.Notebooks).getString(_name)
        PrefManager(_context, PrefGroup.NotebooksData)
                .setString("last_notebook", _name)

        val notebookDataJson = PrefManager(_context, PrefGroup.Notebooks).getString(_name) // Get notebook data as json
        val gson = GsonBuilder().create()
        val notebookData = gson.fromJson(notebookDataJson, NotebookData::class.java) // Convert json and return NotebookData object

        MainActivity.FileList.currentDirectory = notebookData.location
        MainActivity.FileList.currentNotebook  = notebookData

        return notebookData
    }

    fun getNotes(notebookId: String) {
        val syncPref = PrefManager(_context, PrefGroup.Sync)
        val result = SyncManager(_context).getAllNotebookNotes(
                syncPref.getString("userId"),
                syncPref.getString("token"),
                notebookId
        )

        for (note in result.noteSkeletons) {
            Document().add(_context, note.location, note.id)
        }
    }
}