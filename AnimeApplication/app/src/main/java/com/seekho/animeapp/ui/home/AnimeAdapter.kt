package com.seekho.animeapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.seekho.animeapp.data.model.AnimeItem
import com.seekho.animeapp.databinding.ItemAnimeBinding
import com.seekho.animeapp.util.Settings

class AnimeAdapter(
    private val onClick: (AnimeItem) -> Unit
) : ListAdapter<AnimeItem, AnimeAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<AnimeItem>() {
        override fun areItemsTheSame(o: AnimeItem, n: AnimeItem) = o.malId == n.malId
        override fun areContentsTheSame(o: AnimeItem, n: AnimeItem) = o == n
    }

    inner class VH(private val b: ItemAnimeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: AnimeItem) = with(b) {
            title.text = item.title ?: "—"
            episodes.text = "Episodes: " + (item.episodes?.toString() ?: "—")
            rating.text = "Score: " + (item.score?.toString() ?: "—")

            // read current toggle value (Flow snapshot)
            val hide = Settings.hideImages.value
            val initial = (item.title?.trim()?.firstOrNull()?.uppercaseChar() ?: '•').toString()

            if (hide) {
                // show placeholder block (same size), hide real image
                posterPlaceholder.visibility = View.VISIBLE
                posterPlaceholder.text = initial
                poster.visibility = View.INVISIBLE
            } else {
                posterPlaceholder.visibility = View.GONE
                poster.visibility = View.VISIBLE

                val posterUrl = item.images?.webp?.imageUrl ?: item.images?.jpg?.imageUrl
                Glide.with(poster.context)
                    .load(posterUrl)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .into(poster)
            }

            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}