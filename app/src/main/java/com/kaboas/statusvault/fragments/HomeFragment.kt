package com.kaboas.statusvault.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kaboas.statusvault.MainActivity
import com.kaboas.statusvault.PreviewActivity
import com.kaboas.statusvault.R
import com.kaboas.statusvault.adapter.MediaAdapter
import com.kaboas.statusvault.data.AppDatabase
import com.kaboas.statusvault.data.FavoriteEntity
import com.kaboas.statusvault.data.MediaRepository
import com.kaboas.statusvault.data.MediaType
import com.kaboas.statusvault.utils.DownloadHelper
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var adapter: MediaAdapter? = null
    private var recycler: RecyclerView? = null
    private var txtEmpty: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        super.onResume()
        MediaRepository.clearCache()
        loadRecent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOpenWhatsApp = view.findViewById<MaterialButton>(R.id.btnOpenWhatsApp)
        btnOpenWhatsApp.setOnClickListener {
            (activity as? MainActivity)?.openWhatsApp()
        }

        recycler = view.findViewById(R.id.recyclerRecent)
        txtEmpty = view.findViewById(R.id.txtEmptyRecent)

        loadRecent()
    }

    private fun loadRecent() {
        val r = recycler ?: return
        val e = txtEmpty ?: return

        r.layoutManager = GridLayoutManager(requireContext(), 2)

        val allMedia = MediaRepository.listMedia(MediaType.IMAGE) +
                MediaRepository.listMedia(MediaType.VIDEO)
        val recent = allMedia.sortedByDescending { it.lastModified() }.take(6)

        if (recent.isEmpty()) {
            r.visibility = View.GONE
            e.visibility = View.VISIBLE
            return
        }

        r.visibility = View.VISIBLE
        e.visibility = View.GONE

        if (adapter == null) {
            adapter = MediaAdapter(
                recent.toMutableList(),
                favorites = mutableSetOf(),
                onDownload = { file ->
                    val type = if (file.extension.lowercase() in listOf("mp4", "mkv", "3gp", "avi"))
                        MediaType.VIDEO else MediaType.IMAGE
                    DownloadHelper.downloadFile(requireContext(), file, type)
                },
                onFavorite = { file -> toggleFavorite(file) },
                onDelete = { file ->
                    if (file.delete()) {
                        adapter?.removeItem(file)
                        if (adapter?.itemCount == 0) {
                            r.visibility = View.GONE
                            e.visibility = View.VISIBLE
                        }
                        Toast.makeText(requireContext(), "🗑️ Deleted", Toast.LENGTH_SHORT).show()
                    }
                },
                onItemClick = { file ->
                    val intent = Intent(requireContext(), PreviewActivity::class.java)
                    intent.putExtra("file_path", file.absolutePath)
                    startActivity(intent)
                }
            )
            r.adapter = adapter
        } else {
            adapter?.updateData(recent)
        }

        // تحديث قائمة المفضلة
        loadFavoritesForAdapter()
    }

    private fun loadFavoritesForAdapter() {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(requireContext()).favoriteDao()
            dao.getAll().observe(viewLifecycleOwner) { favs ->
                val paths = favs.map { it.path }.toSet()
                adapter?.setFavorites(paths)
            }
        }
    }

    private fun toggleFavorite(file: java.io.File) {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(requireContext()).favoriteDao()
            val isFav = dao.isFavorite(file.absolutePath)
            if (isFav) {
                dao.deleteByPath(file.absolutePath)
                Toast.makeText(requireContext(), "💔 Removed", Toast.LENGTH_SHORT).show()
            } else {
                dao.insert(FavoriteEntity(file.absolutePath, file.name, file.extension))
                Toast.makeText(requireContext(), "❤️ Added", Toast.LENGTH_SHORT).show()
            }
            adapter?.toggleFavorite(file)
        }
    }
}
