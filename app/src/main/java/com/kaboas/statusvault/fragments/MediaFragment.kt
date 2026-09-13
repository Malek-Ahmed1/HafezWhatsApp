package com.kaboas.statusvault.fragments

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isFavorites = arguments?.getBoolean(ARG_FAVORITES) ?: false
        val typeName = arguments?.getString(ARG_TYPE) ?: MediaType.IMAGE.name
        val type = MediaType.valueOf(typeName)

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtCount = view.findViewById<TextView>(R.id.txtCount)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerMedia)
        val txtEmpty = view.findViewById<TextView>(R.id.txtEmpty)

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        val files = if (isFavorites) {
            txtTitle.text = "Favorites"
            // مؤقتاً فاضي لحد ما نضيف Room
            emptyList<File>()
        } else {
            txtTitle.text = if (type == MediaType.VIDEO) "Videos" else "Photos"
            MediaRepository.listMedia(type)
        }

        txtCount.text = files.size.toString()

        if (files.isEmpty()) {
            recycler.visibility = View.GONE
            txtEmpty.visibility = View.VISIBLE
        } else {
            recycler.visibility = View.VISIBLE
            txtEmpty.visibility = View.GONE
            val adapter = MediaAdapter(
                files,
                onDownload = { file ->
                    DownloadHelper.downloadFile(requireContext(), file, type)
                },
                onFavorite = { file ->
                    lifecycleScope.launch {
                        AppDatabase.getInstance(requireContext()).favoriteDao().insert(
                            FavoriteEntity(file.absolutePath, file.name, file.extension)
                        )
                        Toast.makeText(requireContext(), R.string.added_favorite, Toast.LENGTH_SHORT).show()
                    }
                },
                onItemClick = { }
            )
            recycler.adapter = adapter
        }
    }
}
