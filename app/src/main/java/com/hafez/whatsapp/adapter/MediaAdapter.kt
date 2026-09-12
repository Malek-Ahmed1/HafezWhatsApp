package com.hafez.whatsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hafez.whatsapp.R
import java.io.File

class MediaAdapter(
    private var items: List<File>,
    private val onDownload: (File) -> Unit,
    private val onFavorite: (File) -> Unit
) : RecyclerView.Adapter<MediaAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgThumb)
        val btnDownload: Button = v.findViewById(R.id.btnDownload)
        val btnFav: ImageButton = v.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val file = items[position]

        Glide.with(holder.itemView)
            .load(file)
            .centerCrop()
            .into(holder.img)

        holder.btnDownload.setOnClickListener { onDownload(file) }
        holder.btnFav.setOnClickListener { onFavorite(file) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<File>) {
        items = newItems
        notifyDataSetChanged()
    }
}
