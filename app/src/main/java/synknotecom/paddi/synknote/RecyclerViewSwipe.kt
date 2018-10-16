package synknotecom.paddi.synknote

import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import synknotecom.paddi.synknote.Files.Document
import synknotecom.paddi.synknote.Files.Folder

/**
* Created by PaddiM8 on 2/4/18.
*/

fun initializeItemTouchHelper(view: View): ItemTouchHelper.SimpleCallback {
    return object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) : Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            val fromString = MainActivity.FileList.files[fromPosition]
            val toString = MainActivity.FileList.files[toPosition]

            MainActivity.FileList.files[fromPosition] = toString
            MainActivity.FileList.files[toPosition] = fromString
            MainActivity.FileList.adapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val itemId = viewHolder.adapterPosition

            if (direction == ItemTouchHelper.LEFT) {
                if (MainActivity.FileList.files[itemId].isFile)
                    Document(itemId).delete()
                else
                    Folder(itemId).delete()
            } else if (direction == ItemTouchHelper.RIGHT) {
                showRenameDialog(itemId, view)
            }

            MainActivity.FileList.adapter.notifyDataSetChanged()
        }

        override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val paint = Paint()

            if (dX > 0) {
                val rect = RectF(
                        viewHolder.itemView.left.toFloat(),
                        viewHolder.itemView.top.toFloat(),
                        dX,
                        viewHolder.itemView.bottom.toFloat()
                )

                paint.color = Color.parseColor("#43A047")
                canvas.drawRect(rect, paint)
            } else if (dX < 0) {
                val rect = RectF(
                        viewHolder.itemView.right.plus(dX),
                        viewHolder.itemView.top.toFloat(),
                        viewHolder.itemView.right.toFloat(),
                        viewHolder.itemView.bottom.toFloat()
                )

                paint.color = Color.parseColor("#E53935")
                canvas.drawRect(rect, paint)
            }

            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}