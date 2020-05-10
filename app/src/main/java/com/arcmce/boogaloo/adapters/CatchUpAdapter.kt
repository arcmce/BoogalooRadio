package com.arcmce.boogaloo.adapters

import android.content.Intent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.activities.ShowActivity
import com.arcmce.boogaloo.models.RecyclerItem
import kotlinx.android.synthetic.main.catchup_item_layout.view.*
import kotlin.collections.ArrayList

class CatchUpAdapter(private var dataset: ArrayList<RecyclerItem>,
                     val listener: (String) -> Unit):
        androidx.recyclerview.widget.RecyclerView.Adapter<CatchUpAdapter.ViewHolder>() {

    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val name = itemView.tv_show_name
//        val count = itemView.tv_cloudcast_count
        val thumbnail = itemView.iv_show_thumbnail
        var slug: String? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Log.d("CUA", "clicked item position: " + adapterPosition.toString())
            val context = itemView.context
            val showCatchupShowIntent = Intent(context, ShowActivity::class.java)
            showCatchupShowIntent.putExtra("SLUG", slug)
            context.startActivity(showCatchupShowIntent)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val catchupView = LayoutInflater.from(parent.context)
           .inflate(R.layout.catchup_item_layout, parent, false)

        return ViewHolder(catchupView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CUA", "onBindViewHolder " + dataset[position].slug)
        holder.name.text = dataset[position].name
        Glide.with(holder.thumbnail.context)
            .load(dataset[position].thumbnail)
            .apply(requestOptions)
            .into(holder.thumbnail)
        holder.slug = dataset[position].slug

        listener(dataset[position].slug)

    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun updateList(newList: ArrayList<RecyclerItem>) {
        Log.d("CUA", "updateList")
        val diffResult = DiffUtil.calculateDiff(CatchUpDiffCallback(this.dataset, newList))
        dataset = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            Glide.with(holder.thumbnail.context)
                .load(dataset[position].thumbnail)
                .apply(requestOptions)
                .into(holder.thumbnail)
        }
    }
}

class CatchUpDiffCallback(
    private val oldDataset: ArrayList<RecyclerItem>,
    private val newDataset: ArrayList<RecyclerItem>
): DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDataset[oldItemPosition].name == newDataset[newItemPosition].name
    }

    override fun getOldListSize(): Int = oldDataset.size

    override fun getNewListSize(): Int = newDataset.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDataset[oldItemPosition] == newDataset[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val set = mutableSetOf<String>()
        if (oldDataset[oldItemPosition].thumbnail != newDataset[newItemPosition].thumbnail) {
            set.add("thumbnail")
        }
        return set
    }
}

