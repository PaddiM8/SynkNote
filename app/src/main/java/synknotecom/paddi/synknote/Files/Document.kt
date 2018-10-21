package synknotecom.paddi.synknote.Files

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.apache.commons.lang.StringUtils
import org.jasypt.util.text.BasicTextEncryptor
import synknotecom.paddi.synknote.*
import java.io.File
import android.app.ActivityOptions

class Document(documentId: Int = 0, documentFile: File = File("")) {
    private val _documentId = documentId
    private val _documentFile = documentFile

    fun save(context: Context, fileName: String, textEditorComponent: EditText) {
        var documentContent = textEditorComponent.text.toString()

        if (getDefaultPref(context).getBoolean("encryptFilesSwitch", false))
            documentContent = documentContent.encrypt(MainActivity.Protection.encryptionKey)
        File(fixUrl(MainActivity.FileList.currentDirectory) + fileName).writeText(documentContent)
    }

    fun open(view: View) {
        var documentFile = _documentFile
        if (_documentFile == File(""))
            documentFile = MainActivity.FileList.files[_documentId]

        var intent = Intent(view.context, MarkdownEditor::class.java)
        var documentContent = documentFile.readText()

        if (getDefaultPref(view.context).getBoolean("encryptFilesSwitch", false)) {
            val decryptedText = documentContent.decrypt(MainActivity.Protection.encryptionKey)
            if (decryptedText.second || documentContent == "") // Decrypting Failed
            {
                documentContent = decryptedText.first
            } else {
                Toast.makeText(view.context, "Failed to decrypt file.", Toast.LENGTH_LONG).show()
                return
            }
        }

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
        val file = File(directory + fileName)
        file.createNewFile()
        MainActivity.FileList.adapter.add(file)

        //MainActivity.FileList.files.add(File(directory + fileName))
        //MainActivity.FileList.adapter.notifyDataSetChanged()
        Document(0, file).open(view)
    }

    fun delete() {
        val documentFile = MainActivity.FileList.files[_documentId]
        documentFile.delete()
        MainActivity.FileList.adapter.removeItem(_documentId)
    }

    fun rename(newName: String, view: View, mainActivity: MainActivity) {
        val documentFile = MainActivity.FileList.files[_documentId]
        val newFile = File(getSaveLocation(view.context) + newName + "." + documentFile.extension)
        documentFile.renameTo(newFile)
        //MainActivity.FileList.files[_documentId] = newFile
        //MainActivity.FileList.adapter.notifyItemChanged(_documentId)
        mainActivity.loadDocuments()
    }
}