package com.example.boogaloo.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.boogaloo.R
import com.example.boogaloo.models.PlaylistResponse
import kotlinx.android.synthetic.main.catchup_item_layout.view.*

class CatchUpAdapter(private val dataset: PlaylistResponse):
        RecyclerView.Adapter<CatchUpAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name = itemView.tv_show_name
        val thumbnail = itemView.iv_show_thumbnail
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val catchupView = LayoutInflater.from(parent.context)
           .inflate(R.layout.catchup_item_layout, parent, false)
        return ViewHolder(catchupView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CUA", "position: " + position)
        holder.name.text = dataset.data[position].name
        Glide.with(holder.thumbnail.context)
            .load(dataset.data[0].owner.pictures.extraLarge)
            .into(holder.thumbnail)
    }

    override fun getItemCount(): Int {
        return dataset.data.size
    }
}


