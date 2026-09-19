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
import com.kaboas.statusvault.PreviewActivity
import com.kaboas.statusvault.R
import com.kaboas.statusvault.adapter.MediaAdapter
import com.kaboas.statusvault.data.AppDatabase
import com.kaboas.statusvault.data.FavoriteEntity
import com.kaboas.statusvault.data.MediaRepository
import com.kaboas.statusvault.data.MediaType
import com.kaboas.statusvault.utils.DownloadHelper
import kotlinx.coroutines.launch
import java.io.File

class MediaFragment : Fragment() {

    private var adapter: MediaAdapter? = null
    private var isFavorites = false
    private var mediaType = MediaType.IMAGE
    private var recycler: RecyclerView? = null
    private var txtEmpty: TextView? = null

    companion object {
        private const val ARG_TYPE = "type"
        private const val ARG_FAVORITES = "favorites"

        fun newInstance(type: MediaType): MediaFragment {
            val f = MediaFragment()
            val b = Bundle()
            b.putString(ARG_TYPE, type.name)
            b.putBoolean(ARG_FAVORITES, false)
            f.arguments = b
            return f
        }

        fun newInstanceFavorites(): MediaFragment {
            val f = MediaFragment()
            val b = Bundle()
            b.putBoolean(ARG_FAVORITES, true)
            f.arguments = b
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onResume() {
        super.onResume()
        MediaRepository.clearCache()
        loadMedia()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFavorites = arguments?.getBoolean(ARG_FAVORITES) ?: false
        val typeName = arguments?.getString(ARG_TYPE) ?: MediaType.IMAGE.name
        mediaType = MediaType.valueOf(typeName)
        loadMedia()
    }

    private fun loadMedia() {
        val v = view ?: return
        val txtTitle = v.findViewById<TextView>(R.id.txtTitle)
        val txtCount = v.findViewById<TextView>(R.id.txtCount)
        recycler = v.findViewById(R.id.recyclerMedia)
        txtEmpty = v.findViewById(R.id.txtEmpty)

        recycler?.layoutManager = GridLayoutManager(requireContext(), 2)

        if (isFavorites) {
            txtTitle.text = "❤️ Favorites"
            txtEmpty?.text = "❤️ No favorites yet\nTap the heart on any media"
            loadFavorites(txtCount)
        } else {
            txtTitle.text = if (mediaType == MediaType.VIDEO) "🎬 Videos" else "📸 Photos"
            txtEmpty?.text = if (mediaType == MediaType.VIDEO)
                "🎬 No videos yet\nOpen WhatsApp to view statuses"
            else "📸 No photos yet\nOpen WhatsApp to view statuses"
            val files = MediaRepository.listMedia(mediaType)
            txtCount.text = files.size.toString()
            showList(files)
        }
    }

    private fun loadFavorites(txtCount: TextView) {
        val dao = AppDatabase.getInstance(requireContext()).favoriteDao()
        dao.getAll().observe(viewLifecycleOwner) { favs ->
            val files = favs.mapNotNull {
                val f = File(it.path)
                if (f.exists()) f else null
            }
            txtCount.text = files.size.toString()
            showList(files)
        }
    }

    private fun showList(files: List<File>) {
        val r = recycler ?: return
        val e = txtEmpty ?: return

        if (files.isEmpty()) {
            r.visibility = View.GONE
            e.visibility = View.VISIBLE
            return
        }

        r.visibility = View.VISIBLE
        e.visibility = View.GONE

        if (adapter == null) {
            adapter = MediaAdapter(
                files.toMutableList(),
                favorites = mutableSetOf(),
                onDownload = { file ->
                    DownloadHelper.downloadFile(requireContext(), file, mediaType)
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
            adapter?.updateData(files)
        }

        // تحديث قائمة المفضلة
        loadFavoritesForAdapter()
    }

    private fun loadFavoritesForAdapter() {
        val dao = AppDatabase.getInstance(requireContext()).favoriteDao()
        dao.getAll().observe(viewLifecycleOwner) { favs ->
            val paths = favs.map { it.path }.toSet()
            adapter?.setFavorites(paths)
        }
    }

    private fun toggleFavorite(file: File) {
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
