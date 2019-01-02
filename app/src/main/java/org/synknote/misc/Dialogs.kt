package org.synknote.misc

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.rename_dialog.*
import org.synknote.files.Document
import org.synknote.MainActivity
import org.synknote.R
import com.onegravity.rteditor.api.RTApi.getApplicationContext
import android.widget.ArrayAdapter



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
    dialog.setPositiveButton("Create") { _: DialogInterface, _: Int -> }
    dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }

    return dialog.create()
}

@SuppressLint("InflateParams")
fun createRenameDialog(context: Context): AlertDialog {
    val dialog = AlertDialog.Builder(context)
    val dialogView = LayoutInflater.from(context).inflate(R.layout.rename_dialog, null)

    dialogView.findViewById<EditText>(R.id.rename_document_input)
    dialog.setView(dialogView)
    dialog.setCancelable(true)
    dialog.setPositiveButton("Create") { _: DialogInterface, _: Int -> }
    dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }

    return dialog.create()
}

fun showRenameDialog(documentId: Int, view: View, mainActivity: MainActivity) {
    val renameDialog = createRenameDialog(view.context)
    renameDialog.show()

    renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val input = renameDialog.rename_document_input.text.toString()
        val fileExtension = MainActivity.FileList.files[documentId].extension

        // Validation
        when {
            documentExists(input)   -> Toast.makeText(view.context, "File already exists!", Toast.LENGTH_LONG).show()
            input.isEmpty()         -> Toast.makeText(view.context, "File name is too short!", Toast.LENGTH_LONG).show()
            !isValidFileName(input) -> Toast.makeText(view.context, "File name contains bad characters!", Toast.LENGTH_LONG).show()
            else -> Document(documentId).rename(input, view, mainActivity)
        }

        /*val file = File(MainActivity.FileList.currentDirectory + "/" + input + "." + fileExtension)
        MainActivity.FileList.files[documentId] = file.renameTo
        MainActivity.FileList.adapter.notifyDataSetChanged()*/
        Document(documentId).rename("$input.$fileExtension", view, mainActivity)
        renameDialog.dismiss()
    }

    renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
        renameDialog.dismiss()
    }
}

@SuppressLint("InflateParams")
fun createPasswordDialog(context: Context): AlertDialog {
    val dialog = AlertDialog.Builder(context)
    val dialogView = LayoutInflater.from(context).inflate(R.layout.password_dialog, null)

    dialogView.findViewById<EditText>(R.id.password_input)
    dialog.setView(dialogView)
    dialog.setCancelable(false)
    dialog.setPositiveButton("Enter") { _: DialogInterface, _: Int -> }

    return dialog.create()
}

fun newNoteBookDialog(context: Context): AlertDialog {
    val dialog = AlertDialog.Builder(context)
    val dialogView = LayoutInflater.from(context).inflate(R.layout.add_notebook_dialog, null)

    dialog.setView(dialogView)
    dialog.setCancelable(true)
    dialog.setPositiveButton("Add")    { _: DialogInterface, _: Int -> }
    dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }

    return dialog.create()
}