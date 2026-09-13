package com.kaboas.statusvault.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kaboas.statusvault.MainActivity
import com.kaboas.statusvault.R
import com.kaboas.statusvault.adapter.MediaAdapter
import com.kaboas.statusvault.data.MediaRepository
import com.kaboas.statusvault.data.MediaType
import com.kaboas.statusvault.utils.DownloadHelper

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOpenWhatsApp = view.findViewById<MaterialButton>(R.id.btnOpenWhatsApp)
        val btnAddStatus = view.findViewById<MaterialButton>(R.id.btnAddStatus)
        val recyclerRecent = view.findViewById<RecyclerView>(R.id.recyclerRecent)
        val txtEmptyRecent = view.findViewById<TextView>(R.id.txtEmptyRecent)

        btnOpenWhatsApp.setOnClickListener {
            (activity as? MainActivity)?.openWhatsApp()
        }

        btnAddStatus.setOnClickListener {
            // هنضيفها لاحقاً
        }

        recyclerRecent.layoutManager = GridLayoutManager(requireContext(), 2)

        val allMedia = MediaRepository.listMedia(MediaType.IMAGE) +
                MediaRepository.listMedia(MediaType.VIDEO)
        val recent = allMedia.sortedByDescending { it.lastModified() }.take(6)

        if (recent.isEmpty()) {
            recyclerRecent.visibility = View.GONE
            txtEmptyRecent.visibility = View.VISIBLE
        } else {
            recyclerRecent.visibility = View.VISIBLE
            txtEmptyRecent.visibility = View.GONE
            val adapter = MediaAdapter(
                recent,
                onDownload = { file ->
                    val type = if (file.extension.lowercase() in listOf("mp4", "mkv", "3gp", "avi"))
                        MediaType.VIDEO else MediaType.IMAGE
                    DownloadHelper.downloadFile(requireContext(), file, type)
                },
                onFavorite = { },
                onItemClick = { }
            )
            recyclerRecent.adapter = adapter
        }
    }
}
