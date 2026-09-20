package com.kaboas.statusvault.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kaboas.statusvault.R
import java.io.File

class MediaAdapter(
    private var items: MutableList<File>,
    private val favorites: MutableSet<String> = mutableSetOf(),
    private val isFavoritesTab: Boolean = false,
    private val onDownload: (File) -> Unit,
    private val onFavorite: (File) -> Unit,
    private val onDelete: (File) -> Unit,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<MediaAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgThumb)
        val btnDownload: ImageButton = v.findViewById(R.id.btnDownload)
        val btnFav: ImageButton = v.findViewById(R.id.btnFavorite)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDelete)
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

        val isFav = favorites.contains(file.absolutePath)

        if (isFavoritesTab) {
            // في تاب المفضلة: الزر دايماً ظاهر — بيستخدم love.png للإزالة
            holder.btnFav.visibility = View.VISIBLE
            holder.btnFav.setImageResource(R.drawable.love)
        } else {
            // في التابات التانية: الزر يظهر بس لو مش في المفضلة — بيستخدم heart_empty.png
            holder.btnFav.visibility = if (isFav) View.GONE else View.VISIBLE
            holder.btnFav.setImageResource(R.drawable.heart_empty)
        }

        holder.btnDownload.setOnClickListener { onDownload(file) }
        holder.btnFav.setOnClickListener { onFavorite(file) }
        holder.btnDelete.setOnClickListener { onDelete(file) }
        holder.itemView.setOnClickListener { onItemClick(file) }
    }

    override fun getItemCount() = items.size

    fun removeItem(file: File) {
        val index = items.indexOf(file)
        if (index >= 0) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateData(newItems: List<File>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun setFavorites(newFavs: Set<String>) {
        favorites.clear()
        favorites.addAll(newFavs)
        notifyDataSetChanged()
    }

    fun toggleFavorite(file: File) {
        if (favorites.contains(file.absolutePath)) {
            favorites.remove(file.absolutePath)
        } else {
            favorites.add(file.absolutePath)
        }
        val index = items.indexOf(file)
        if (index >= 0) notifyItemChanged(index)
    }
}
