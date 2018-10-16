package synknotecom.paddi.synknote

import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.io.File
import synknotecom.paddi.synknote.Files.Document
import synknotecom.paddi.synknote.Files.Folder


/**
* Created by PaddiM8 on 1/31/18.
*/

class Adapter(private val fileList: ArrayList<File>, private val mainActivity: MainActivity) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_row, parent, false)
        return ViewHolder(v).onClick { i: Int, _: Int ->
            if (fileList[i].isFile)
                Document(i).open(parent.rootView)
            else // If it's a folder
                Folder(i).open(mainActivity)
        }
    }

    override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
        holder.bindItems(fileList[position], position)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun removeItem(position: Int) {
        fileList.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(file: File, fileId: Int) {


            var fileIcon = R.drawable.ic_file
            var folderIcon = R.drawable.ic_folder
            if (getDefaultPref(itemView.context).getBoolean("darkThemeSettingsCheckbox", false)) {
                fileIcon = R.drawable.ic_file_dark
                folderIcon = R.drawable.ic_folder_dark
            }

            // Icon
            if (file.isFile)
                itemView.image_view_document_icon
                        .setImageDrawable(ContextCompat.getDrawable(itemView.context, fileIcon))
            else
                itemView.image_view_document_icon
                        .setImageDrawable(ContextCompat.getDrawable(itemView.context, folderIcon))

            itemView.text_view_document_title.text = file.nameWithoutExtension
            itemView.text_view_date.text = getDate(file.lastModified(), "dd/MM")
            itemView.more_button.setOnClickListener {
                val popupMenu = PopupMenu(itemView.context, itemView, Gravity.END)

                popupMenu.inflate(R.menu.menu_recyclerview_item)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.renameButton -> showRenameDialog(fileId, itemView)
                        R.id.deleteButton -> {
                            if (file.isFile)
                                Document(fileId).delete()
                            else  Folder(fileId).delete()
                        }
                        R.id.pinButton -> {
                            if (file.isFile)
                                Document(fileId).rename("." + file.name, itemView)
                            else  Folder(fileId).rename("." + file.name)
                        }
                    }
                    true
                }

                popupMenu.show()

            }
        }
    }
}