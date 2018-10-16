package synknotecom.paddi.synknote.Files

import android.view.View
import synknotecom.paddi.synknote.MainActivity
import synknotecom.paddi.synknote.fixUrl
import java.io.File

class Folder(folderId: Int = 0) {
    private val _folderId = folderId

    fun create(name: String) {
        val folder = File(fixUrl(MainActivity.FileList.currentDirectory) + name)
        folder.mkdirs()
        MainActivity.FileList.files.add(0, folder)
        MainActivity.FileList.adapter.notifyItemInserted(0)
    }

    fun delete() {
        val folder = MainActivity.FileList.files[_folderId]
        folder.deleteRecursively()
        MainActivity.FileList.adapter.removeItem(_folderId)
    }

    fun rename(newName: String) {
        val documentFile = MainActivity.FileList.files[_folderId]
        val newFile = File(MainActivity.FileList.currentDirectory + newName + "." + documentFile.extension)
        documentFile.renameTo(newFile)
        MainActivity.FileList.files[_folderId] = newFile
    }

    fun open(mainActivity: MainActivity) {
        MainActivity.FileList.currentDirectory = MainActivity.FileList.files[_folderId].path
        mainActivity.loadFileList()
        mainActivity.loadDocuments()
    }
}