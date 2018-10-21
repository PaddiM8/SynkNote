package synknotecom.paddi.synknote

import android.opengl.Visibility
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.io.File
import synknotecom.paddi.synknote.Files.Document
import synknotecom.paddi.synknote.Files.Folder
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities


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
            if (fileName.startsWith(".")) {
                fileName = fileName.substring(1)
                itemView.pin.visibility = View.VISIBLE
            }

            itemView.text_view_document_title.text = fileName
            itemView.text_view_date.text = getDate(file.lastModified(), "dd/MM")
            itemView.more_button.setOnClickListener { _ ->
                val popupMenu = PopupMenu(itemView.context, itemView, Gravity.END)
                var newPinName = ".$originalFileName"
                popupMenu.inflate(R.menu.menu_recyclerview_item)

                if (originalFileName.startsWith(".")) {
                    popupMenu.menu.getItem(2).title = "Unpin"
                    newPinName = originalFileName.substring(1)
                }

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.renameButton -> showRenameDialog(fileId, itemView, mainActivity)
                        R.id.deleteButton -> {
                            if (file.isFile)
                                Document(fileId).delete()
                            else  Folder(fileId).delete()
                        }
                        R.id.pinButton -> {
                            if (file.isFile) {
                                Document(fileId).rename(newPinName, itemView, mainActivity)
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