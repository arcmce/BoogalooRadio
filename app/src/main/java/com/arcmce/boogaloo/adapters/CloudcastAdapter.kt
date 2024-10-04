package com.arcmce.boogaloo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.databinding.CloudcastItemLayoutBinding
import com.arcmce.boogaloo.models.CloudcastRecyclerItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


class CloudcastAdapter(private var dataset: ArrayList<CloudcastRecyclerItem>,
                       private val listener: (CloudcastRecyclerItem) -> Unit) :
    RecyclerView.Adapter<CloudcastAdapter.ViewHolder>() {

    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    class ViewHolder(binding: CloudcastItemLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        val name = binding.tvCloudcastName
        val thumbnail = binding.ivCloudcastThumbnail
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CloudcastItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
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

