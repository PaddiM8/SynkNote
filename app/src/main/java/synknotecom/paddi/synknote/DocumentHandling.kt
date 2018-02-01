package synknotecom.paddi.synknote

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_editor.*
import java.io.File


/**
* Created by PaddiM8 on 1/30/18.
*/

fun saveDocument(context: Context, fileName: String, textEditorComponent: EditText) {
    val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    fos.write(textEditorComponent.text.toString().toByteArray())
    fos.close()
}

fun openDocument(id: Int, view: View) {
    val documentFile = MainWindow.FileList.files[id]
    var intent = Intent(view.context, Editor::class.java)

    if (documentFile.extension == "txt")
        intent = Intent(view.context, NormalEditor::class.java)

    intent.putExtra("title", documentFile.nameWithoutExtension)
    intent.putExtra("content", documentFile.readText())
    intent.putExtra("filename", documentFile.name)
    view.context.startActivity(intent)
}

fun createDocument(name: String, type: String, view: View) {
    val fileName = name + getFileExtensionFromType(type)
    val fos = view.context.openFileOutput(fileName, Context.MODE_PRIVATE)
    fos.write("".toByteArray()) // Create empty file
    fos.close()

    MainWindow.FileList.files.add(File(view.context.applicationInfo.dataDir + "/files/" + fileName))
    MainWindow.FileList.adapter.notifyDataSetChanged()
    openDocument(MainWindow.FileList.files.count() - 1, view)
}