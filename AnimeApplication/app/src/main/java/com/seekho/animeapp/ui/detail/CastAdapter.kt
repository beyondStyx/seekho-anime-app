package com.seekho.animeapp.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seekho.animeapp.data.model.AnimeCharacter
import com.seekho.animeapp.databinding.ItemCastBinding
import com.seekho.animeapp.util.Settings

class CastAdapter : RecyclerView.Adapter<CastAdapter.VH>() {

    private val items = mutableListOf<AnimeCharacter>()

    fun submit(list: List<AnimeCharacter>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemCastBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: AnimeCharacter) {
            b.name.text = item.character.name ?: "—"

            val hide = Settings.hideImages.value
            val initial = (item.character.name?.trim()?.firstOrNull()?.uppercaseChar() ?: '•').toString()

            if (hide) {
                b.imagePlaceholder.visibility = View.VISIBLE
                b.imagePlaceholder.text = initial
                b.image.visibility = View.INVISIBLE
            } else {
                b.imagePlaceholder.visibility = View.GONE
                b.image.visibility = View.VISIBLE
                val url = item.character.images?.jpg?.imageUrl
                Glide.with(b.image)
                    .load(url)
                    .circleCrop()
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .into(b.image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}