package org.synknote.files

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.gson.GsonBuilder
import java.io.File
import org.synknote.*
import org.synknote.misc.*
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager
import org.synknote.sync.SyncManager

class Document(documentId: Int = 0, documentFile: File = File("")) {
    private val _documentId = documentId
    private val _documentFile = documentFile

    fun save(context: Context, fileName: String, noteId: String, textEditorComponent: EditText, sync: Boolean) {
        var documentContent = textEditorComponent.text.toString()

        //if (getDefaultPref(context).getBoolean("encrypt_files_switch", false)) {
            documentContent = documentContent.encrypt(MainActivity.Protection.encryptionKey)
        //}

        if (MainActivity.FileList.currentNotebook.sync)
            documentContent += noteId

        File(fixUrl(MainActivity.FileList.currentDirectory) + fileName)
                                                      .writeText(documentContent)

        if (sync) {
            val syncPref = PrefManager(context, PrefGroup.Sync)
            SyncManager(context).editNote(
                    syncPref.getString("userId"),
                    syncPref.getString("token"),
                    noteId,
                    documentContent
            )
        }
    }

    fun open(view: View, new: Boolean = false) {
        var documentFile = _documentFile
        if (_documentFile == File(""))
            documentFile = MainActivity.FileList.files[_documentId]

        var intent = Intent(view.context, MarkdownEditor::class.java)
        var documentContent = documentFile.readText()
        var noteId = ""
        val sync = MainActivity.FileList.currentNotebook.sync

        if (sync && !new) {
            noteId = documentContent.substring(documentContent.length - 24)

            val syncPref = PrefManager(view.context, PrefGroup.Sync)
            val result = SyncManager(view.context).getNote(
                    syncPref.getString("userId"),
                    syncPref.getString("token"),
                    noteId
            )

            //syncPref.setString("token", result["token"].toString())
            documentContent = result["content"].toString()
                    .substring(0, documentContent.length - 24)
        } else if (sync && new) {
            noteId = documentContent
            documentContent = ""
        }

        //if (getDefaultPref(view.context).getBoolean("encrypt_files_switch", false)) {
            val decryptedText = documentContent.decrypt(MainActivity.Protection.encryptionKey)
            if (decryptedText.second || documentContent == "") // Decrypting Succeeded
            {
                documentContent = decryptedText.first
            } else {
                Toast.makeText(view.context, "Failed to decrypt file.", Toast.LENGTH_LONG).show()
                return
            }
        //}

        if (documentFile.extension == "txt")
            intent = Intent(view.context, NormalEditor::class.java)

        MainActivity.Protection.askForPassword = false

        intent.putExtra("noteId", noteId)
        intent.putExtra("title", documentFile.nameWithoutExtension)
        intent.putExtra("content", documentContent)
        intent.putExtra("filename", documentFile.name)
        view.context.startActivity(intent)
    }

    fun create(name: String, type: String, view: View, sync: Boolean) {
        val fileName = name + getFileExtensionFromType(type)
        val directory = fixUrl(MainActivity.FileList.currentDirectory)
        File(directory).mkdirs()
        val file = File(directory + fileName)
        file.createNewFile()
        MainActivity.FileList.adapter.add(file)

        // Sync
        if (sync) {
            val syncPref = PrefManager(view.context, PrefGroup.Sync)
            val notebookName = PrefManager(view.context, PrefGroup.NotebooksData)
                    .getString("last_notebook")
            val notebook = PrefManager(view.context, PrefGroup.Notebooks).getString(notebookName)
            val notebookData = GsonBuilder().create().fromJson(notebook, NotebookData::class.java)
            val path = file.canonicalPath.substring(view.context.filesDir.canonicalPath.length)

            val result = SyncManager(view.context).createNote(
                    syncPref.getString("userId"),
                    syncPref.getString("token"),
                    path,
                    notebookData.id
            )

            //syncPref.setString("token", result["token"].toString())
            file.writeText(result["id"].toString())
        }
        //MainActivity.FileList.files.add(File(directory + fileName))
        //MainActivity.FileList.adapter.notifyDataSetChanged()
        Document(0, file).open(view, true)
    }

    fun add(context: Context, location: String, id: String) {
        val file = File(fixUrl(context.filesDir.canonicalPath) + location)
        file.writeText(id)
    }

    fun delete() {
        val documentFile = MainActivity.FileList.files[_documentId]
        documentFile.delete()
        MainActivity.FileList.adapter.removeItem(_documentId)
    }

    fun rename(newName: String, view: View, mainActivity: MainActivity) {
        val documentFile = MainActivity.FileList.files[_documentId]
        val newFile = File(getSaveLocation(view.context) + newName + "" + documentFile.extension)
        documentFile.renameTo(newFile)
        //MainActivity.FileList.files[_documentId] = newFile
        //MainActivity.FileList.adapter.notifyItemChanged(_documentId)
        mainActivity.loadDocuments()
    }
}