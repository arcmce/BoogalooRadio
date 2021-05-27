package com.arcmce.boogaloo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.models.CloudcastRecyclerItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.cloudcast_item_layout.view.*


class CloudcastAdapter(private var dataset: ArrayList<CloudcastRecyclerItem>,
                       private val listener: (CloudcastRecyclerItem) -> Unit) :
    RecyclerView.Adapter<CloudcastAdapter.ViewHolder>() {

    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name = itemView.tv_cloudcast_name
        val thumbnail = itemView.iv_cloudcast_thumbnail
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val catchupView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cloudcast_item_layout, parent, false)

        return ViewHolder(catchupView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]

        Log.d("CCA", "onBindViewHolder " + item.name)

        holder.name.text = item.name
        Glide.with(holder.thumbnail.context)
            .load(item.thumbnail)
            .apply(requestOptions)
            .into(holder.thumbnail)

        holder.itemView.setOnClickListener { listener(item) }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}

