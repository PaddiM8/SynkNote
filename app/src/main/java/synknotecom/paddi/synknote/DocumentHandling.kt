package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import org.apache.commons.lang.StringUtils
import org.jasypt.util.text.BasicTextEncryptor
import java.io.File


/**
* Created by PaddiM8 on 1/30/18.
*/

fun saveDocument(context: Context, fileName: String, textEditorComponent: EditText) {
    var documentContent = textEditorComponent.text.toString()

    if (getDefaultPref(context).getBoolean("encryptFilesSwitch", false))
        documentContent = encryptString(documentContent, MainActivity.Protection.encryptionKey)
    File(fixUrl(MainActivity.FileList.currentDirectory) + fileName).writeText(documentContent)
}

fun openDocument(id: Int, view: View) {
    val documentFile = MainActivity.FileList.files[id]
    var intent = Intent(view.context, MarkdownEditor::class.java)
    var documentContent = documentFile.readText()
    documentContent = decryptString(documentContent, MainActivity.Protection.encryptionKey)

    if (documentFile.extension == "txt")
        intent = Intent(view.context, NormalEditor::class.java)

    MainActivity.Protection.askForPassword = false

    intent.putExtra("title", documentFile.nameWithoutExtension)
    intent.putExtra("content", documentContent)
    intent.putExtra("filename", documentFile.name)
    view.context.startActivity(intent)
}

fun createDocument(name: String, type: String, view: View) {
    val fileName = name + getFileExtensionFromType(type)
    val directory = fixUrl(MainActivity.FileList.currentDirectory)
    File(directory).mkdirs()
    File(directory + fileName).createNewFile()

    MainActivity.FileList.files.add(File(directory + fileName))
    MainActivity.FileList.adapter.notifyDataSetChanged()
    openDocument(MainActivity.FileList.files.count() - 1, view)
}

fun createFolder(name: String) {
    val folder = File(fixUrl(MainActivity.FileList.currentDirectory) + name)
    folder.mkdirs()
    MainActivity.FileList.files.add(0, folder)
    MainActivity.FileList.adapter.notifyItemInserted(0)
}

fun openFolder(id: Int, mainActivity: MainActivity) {
    MainActivity.FileList.currentDirectory = MainActivity.FileList.files[id].path
    mainActivity.loadFileList()
    mainActivity.loadDocuments()
}

fun deleteDocument(id: Int) {
    val documentFile = MainActivity.FileList.files[id]
    documentFile.delete()
    MainActivity.FileList.adapter.removeItem(id)
}

fun renameDocument(id: Int, newName: String) {
    val documentFile = MainActivity.FileList.files[id]
    val newFile = File(MainActivity.FileList.currentDirectory + newName + "." + documentFile.extension)
    documentFile.renameTo(newFile)
    MainActivity.FileList.files[id] = newFile
}

fun encryptString(input: String, key: String): String {
    return try {
        val textEncryptor = BasicTextEncryptor()
        textEncryptor.setPassword(key)
        textEncryptor.encrypt(input)
    } catch (e: Exception) {
        input
    }
}

fun decryptString(input: String, key: String): String {
    return try {
        val textEncryptor = BasicTextEncryptor()
        textEncryptor.setPassword(key)
        return textEncryptor.decrypt(input)
    } catch (e: Exception) {
        e.toString()
    }
}

fun documentExists(documentName: String) : Boolean {
    val fileLocation = MainActivity.FileList.currentDirectory + documentName
    return File(fileLocation).exists()
}

fun getSaveLocation(context: Context): String {
    return getDefaultPref(context).getString("localFolderEditText", null)
}

fun isValidFileName(fileName: String): Boolean {
    return !StringUtils.containsAny(fileName, "|\\?*<\\\":>/'")
}