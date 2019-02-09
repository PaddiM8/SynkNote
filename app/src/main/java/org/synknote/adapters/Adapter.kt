package org.synknote.adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import org.synknote.files.Document
import org.synknote.files.Folder
import org.synknote.MainActivity
import org.synknote.misc.Icons
import org.synknote.misc.getDate
import org.synknote.misc.onClick
import org.synknote.misc.showRenameDialog
import org.synknote.R
import org.synknote.ThemeManager
import java.io.File


/**
 * Created by PaddiM8 on 1/31/18.
 */

class Adapter(private val fileList: ArrayList<File>, private val mainActivity: MainActivity) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_row, parent, false)
        return ViewHolder(v).onClick { i: Int, _: Int ->
            if (fileList[i].isFile)
                Document(i).open(parent.rootView)
            else // If it's a folder
                Folder(i).open(mainActivity)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(fileList[position], position, mainActivity)
    }

    fun add(file: File) {
        fileList.add(0, file)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun removeItem(position: Int) {
        fileList.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(file: File, fileId: Int, mainActivity: MainActivity) {

            // Icon
            val themeManager = ThemeManager(itemView.context)
            var icon = themeManager.getIcon(Icons.FILE)
            if (file.isDirectory)
                icon = themeManager.getIcon(Icons.FOLDER)

            itemView.image_view_document_icon
                    .setImageDrawable(ContextCompat.getDrawable(itemView.context, icon))

            val originalFileName = file.nameWithoutExtension
            var fileName = originalFileName
            val isPinned = fileName.startsWith(".")

            if (isPinned) {
                fileName = fileName.substring(1)
                itemView.pin.visibility = View.VISIBLE
            }

            itemView.text_view_document_title.text = fileName
            itemView.text_view_date.text = getDate(file.lastModified(), "dd/MM")
            itemView.more_button.setOnClickListener { _ ->
                val popupMenu = PopupMenu(itemView.context, itemView, Gravity.END)
                var newPinName = ".$originalFileName"
                popupMenu.inflate(R.menu.menu_recyclerview_item)

                if (isPinned) {
                    popupMenu.menu.getItem(2).title = "Unpin"
                    newPinName = originalFileName.substring(1)
                }

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.rename_button -> showRenameDialog(fileId, itemView, mainActivity)
                        R.id.delete_button -> {
                            if (file.isFile) {
                                mainActivity.refresh()
                                Document(fileId).delete(file.canonicalPath, mainActivity)
                            } else {
                                Folder(fileId).delete()
                            }
                        }
                        R.id.pin_button -> {
                            if (file.isFile) {
                                Document(fileId).rename(mainActivity, newPinName, itemView)
                            } else {
                                Folder(fileId).rename(newPinName, mainActivity)
                            }
                        }
                    }
                    true
                }

                popupMenu.show()

            }
        }
    }
}