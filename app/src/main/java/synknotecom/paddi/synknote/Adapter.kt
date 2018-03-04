package synknotecom.paddi.synknote

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.io.File


/**
* Created by PaddiM8 on 1/31/18.
*/

class Adapter(private val fileList: ArrayList<File>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_row, parent, false)
        return ViewHolder(v).onClick { i: Int, _: Int ->
            openDocument(i, parent.rootView)
        }
    }

    override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
        holder.bindItems(fileList[position])
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun removeItem(position: Int) {
        fileList.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(file: File) {
            // Icon
            if (file.isFile)
                itemView.image_view_document_icon.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_file))
            else
                itemView.image_view_document_icon.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_folder))

            itemView.text_view_document_title.text = file.nameWithoutExtension
            itemView.text_view_date.text = getDate(file.lastModified(), "dd/MM")
        }
    }
}