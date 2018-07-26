package synknotecom.paddi.synknote.Files

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import org.apache.commons.lang.StringUtils
import org.jasypt.util.text.BasicTextEncryptor
import synknotecom.paddi.synknote.*
import java.io.File

class Document(documentId: Int = 0) {
    private val _documentId = documentId

    fun save(context: Context, fileName: String, textEditorComponent: EditText) {
        var documentContent = textEditorComponent.text.toString()

        if (getDefaultPref(context).getBoolean("encryptFilesSwitch", false))
            documentContent = encryptString(documentContent, MainActivity.Protection.encryptionKey)
        File(fixUrl(MainActivity.FileList.currentDirectory) + fileName).writeText(documentContent)
    }

    fun open(view: View) {
        val documentFile = MainActivity.FileList.files[_documentId]
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

    fun create(name: String, type: String, view: View) {
        val fileName = name + getFileExtensionFromType(type)
        val directory = fixUrl(MainActivity.FileList.currentDirectory)
        File(directory).mkdirs()
        File(directory + fileName).createNewFile()

        MainActivity.FileList.files.add(File(directory + fileName))
        MainActivity.FileList.adapter.notifyDataSetChanged()
        openDocument(MainActivity.FileList.files.count() - 1, view)
    }

    fun delete() {
        val documentFile = MainActivity.FileList.files[_documentId]
        documentFile.delete()
        MainActivity.FileList.adapter.removeItem(_documentId)
    }

    fun rename(newName: String, view: View) {
        val documentFile = MainActivity.FileList.files[_documentId]
        val newFile = File(getSaveLocation(view.context) + newName + "." + documentFile.extension)
        documentFile.renameTo(newFile)
        MainActivity.FileList.files[_documentId] = newFile
    }

    private fun encryptString(input: String, key: String): String {
        return try {
            val textEncryptor = BasicTextEncryptor()
            textEncryptor.setPassword(key)
            textEncryptor.encrypt(input)
        } catch (e: Exception) {
            input
        }
    }

    private fun decryptString(input: String, key: String): String {
        return try {
            val textEncryptor = BasicTextEncryptor()
            textEncryptor.setPassword(key)

            if (input != "")
                textEncryptor.decrypt(input)
            else
                ""
        } catch (e: Exception) {
            input
        }
    }
}