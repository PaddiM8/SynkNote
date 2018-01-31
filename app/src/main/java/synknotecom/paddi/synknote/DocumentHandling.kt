package synknotecom.paddi.synknote

import android.content.Context
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_editor.*


/**
 * Created by paddi on 1/30/18.
 */

fun saveDocument(context: Context, fileName: String, textEditorComponent: EditText) {
    //val fileName = fileName //intent.getStringExtra("filename") //title.toString() + getFileExtensionFromType(documentType)
    val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    fos.write(textEditorComponent.text.toString().toByteArray())
    fos.close()
}
