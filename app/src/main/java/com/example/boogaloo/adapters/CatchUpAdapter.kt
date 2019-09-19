package com.example.boogaloo.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.name.text = dataset.data[p1].name
    }

    override fun getItemCount(): Int {
        return dataset.data.size
    }
}


