package org.synknote.files

import org.synknote.MainActivity
import org.synknote.misc.fixUrl
import java.io.File

class Folder(folderId: Int = 0) {
    private val _folderId = folderId

    fun create(name: String) {
        val folder = File(fixUrl(MainActivity.FileList.currentDirectory) + name)
        folder.mkdirs()

        MainActivity.FileList.adapter.add(folder)
        //MainActivity.FileList.adapter.notifyDataSetChanged()
    }

    fun delete() {
        val folder = MainActivity.FileList.files[_folderId]
        folder.deleteRecursively()
        MainActivity.FileList.adapter.removeItem(_folderId)
    }

    fun rename(newName: String, mainActivity: MainActivity) {
        val documentFile = MainActivity.FileList.files[_folderId]
        val newFile = File(MainActivity.FileList.currentDirectory + newName + "" + documentFile.extension)
        documentFile.renameTo(newFile)
        //MainActivity.FileList.files[_folderId] = newFile
        //MainActivity.FileList.adapter.notifyItemChanged(_folderId)
        mainActivity.loadDocuments()
    }

    fun open(mainActivity: MainActivity) {
        MainActivity.FileList.currentDirectory = MainActivity.FileList.files[_folderId].path
        mainActivity.loadDocuments()
    }
}