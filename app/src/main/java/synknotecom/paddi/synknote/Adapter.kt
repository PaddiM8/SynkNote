package synknotecom.paddi.synknote

import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.io.File
import android.view.ContextMenu.ContextMenuInfo




/**
* Created by PaddiM8 on 1/31/18.
*/

class Adapter(private val fileList: ArrayList<File>, private val mainActivity: MainActivity) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_row, parent, false)
        return ViewHolder(v).onClick { i: Int, _: Int ->
            if (fileList[i].isFile)
                openDocument(i, parent.rootView)
            else // If it's a folder
                openFolder(i, mainActivity)
        }
    }

    override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
        holder.bindItems(fileList[position], fileList)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun removeItem(position: Int) {
        fileList.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(file: File, fileList: ArrayList<File>) {


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
                val popupMenu = PopupMenu(itemView.context, itemView)
                val itemId = itemView.id

                popupMenu.inflate(R.menu.menu_recyclerview_item)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.renameButton -> showRenameDialog(itemId, itemView)
                        R.id.deleteButton -> {
                            if (fileList[itemId].isFile)
                                deleteDocument(itemId)
                            else deleteFolder(itemId)
                        }
                        R.id.pinButton -> Log.d("Pin", "pin!") // TODO: Ability to pin item
                    }
                    true
                }

                popupMenu.show()

            }
        }
    }
}