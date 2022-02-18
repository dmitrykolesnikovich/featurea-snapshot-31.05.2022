package featurea.android.simulator

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import featurea.nameWithoutExtension
import java.io.File

class FilesAdapter(private val files: MutableList<File>) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    var onClickListener: ItemClickListener<File>? = null
    var onLongClickListener: ItemClickListener<File>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder = FileViewHolder(parent)

    override fun onBindViewHolder(viewHolder: FileViewHolder, position: Int) {
        val file = files[position]
        viewHolder.itemView.setBackgroundColor(Color.WHITE)
        viewHolder.titleTextView.visibility = View.VISIBLE
        viewHolder.titleTextView.text = file.absolutePath.nameWithoutExtension
    }

    override fun getItemCount(): Int = files.size

    fun remove(position: Int) {
        val file = files[position]
        if (files.contains(file)) {
            notifyItemRemoved(position)
            files.removeAt(position)
        }
    }

    inner class FileViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.bundle_item, parent, false)),
        View.OnClickListener, View.OnLongClickListener {

        val titleTextView: TextView = itemView.findViewById(R.id.nameTextView)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            val onClickListener = onClickListener ?: return
            onClickListener.onClickItem(files[adapterPosition])
        }

        override fun onLongClick(v: View): Boolean {
            val onLongClickListener = onLongClickListener ?: return false
            onLongClickListener.onClickItem(files[adapterPosition])
            return true
        }

    }

}
