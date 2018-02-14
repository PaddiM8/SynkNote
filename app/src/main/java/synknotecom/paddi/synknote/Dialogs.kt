package synknotecom.paddi.synknote

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.new_document_dialog.*
import kotlinx.android.synthetic.main.rename_dialog.*
import synknotecom.paddi.synknote.R.id.rename_document_input
import java.io.File

/**
 * Created by PaddiM8 on 2/4/18.
 */

@SuppressLint("InflateParams")
fun createNewDocumentDialog(context: Context): AlertDialog {
    val dialog = AlertDialog.Builder(context)
    val dialogView = LayoutInflater.from(context).inflate(R.layout.new_document_dialog, null)

    dialogView.findViewById<EditText>(R.id.document_name_input)
    dialog.setView(dialogView)
    dialog.setCancelable(true)
    dialog.setPositiveButton("Create", { _: DialogInterface, _: Int -> })
    dialog.setNegativeButton("Cancel", { _: DialogInterface, _: Int -> })

    return dialog.create()
}

fun createRenameDialog(context: Context): AlertDialog {
    val dialog = AlertDialog.Builder(context)
    val dialogView = LayoutInflater.from(context).inflate(R.layout.rename_dialog, null)

    dialogView.findViewById<EditText>(R.id.rename_document_input)
    dialog.setView(dialogView)
    dialog.setCancelable(true)
    dialog.setPositiveButton("Create", { _: DialogInterface, _: Int -> })
    dialog.setNegativeButton("Cancel", { _: DialogInterface, _: Int -> })

    return dialog.create()
}

fun showRenameDialog(documentId: Int, view: View) {
    val renameDialog = createRenameDialog(view.context)
    renameDialog.show()

    renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
        var input = renameDialog.rename_document_input.text.toString()
        var fileExtension = MainActivity.FileList.files[documentId].extension

        renameDocument(documentId, input, view)
        MainActivity.FileList.files[documentId] = File(getFileDirectory(view.context) + input + "." + fileExtension)
        //MainActivity.FileList.adapter.notifyDataSetChanged()
        MainActivity.FileList.adapter = Adapter(MainActivity.FileList.files) // TODO: MAKE MORE EFFICIENT DAMN KIT!
        renameDialog.dismiss()
    })

    renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
        renameDialog.dismiss()
    })
}

fun createPasswordDialog(context: Context): AlertDialog {
    val dialog = AlertDialog.Builder(context)
    val dialogView = LayoutInflater.from(context).inflate(R.layout.password_dialog, null)

    dialogView.findViewById<EditText>(R.id.password_input)
    dialog.setView(dialogView)
    dialog.setCancelable(false)
    dialog.setPositiveButton("Enter", { _: DialogInterface, _: Int -> })

    return dialog.create()
}