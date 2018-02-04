package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.view.View
import android.widget.EditText
import org.jasypt.util.text.BasicTextEncryptor
import java.io.File


/**
* Created by PaddiM8 on 1/30/18.
*/

fun saveDocument(context: Context, fileName: String, textEditorComponent: EditText) {
    var documentContent = textEditorComponent.text.toString()

    documentContent = encryptString(documentContent)

    val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    fos.write(documentContent.toByteArray())
    fos.close()
}

fun openDocument(id: Int, view: View) {
    val documentFile = MainActivity.FileList.files[id]
    var intent = Intent(view.context, MarkdownEditor::class.java)
    var documentContent = documentFile.readText()

    documentContent = decryptString(documentContent)

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
    val fos = view.context.openFileOutput(fileName, Context.MODE_PRIVATE)
    fos.write("".toByteArray()) // Create empty file
    fos.close()

    MainActivity.FileList.files.add(File(view.context.applicationInfo.dataDir + "/files/" + fileName))
    MainActivity.FileList.adapter.notifyDataSetChanged()
    openDocument(MainActivity.FileList.files.count() - 1, view)
}

fun deleteDocument(id: Int) {
    val documentFile = MainActivity.FileList.files[id]
    documentFile.delete()
    MainActivity.FileList.adapter.removeItem(id)
}

fun renameDocument(id: Int, newName: String, view: View) {
    val documentFile = MainActivity.FileList.files[id]
    val newFile = File(view.context.applicationInfo.dataDir + "/files/" + newName + "." + documentFile.extension)
    documentFile.renameTo(newFile)
    MainActivity.FileList.files[id] = newFile
}

fun encryptString(input: String): String {
    return try {
        val textEncryptor = BasicTextEncryptor()
        textEncryptor.setPassword(MainActivity.Protection.encryptionKey)
        textEncryptor.encrypt(input)
    } catch (e: Exception) {
        input
    }
}

fun decryptString(input: String): String {
    return try {
        val textEncryptor = BasicTextEncryptor()
        textEncryptor.setPassword(MainActivity.Protection.encryptionKey)
        return textEncryptor.decrypt(input)
    } catch (e: Exception) {
        input
    }
}