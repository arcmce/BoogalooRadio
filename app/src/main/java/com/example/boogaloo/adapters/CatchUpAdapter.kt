package com.example.boogaloo.adapters

import android.content.Intent
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.boogaloo.R
import com.example.boogaloo.VolleySingleton
import com.example.boogaloo.activities.ShowActivity
import com.example.boogaloo.models.CloudcastResponse
import com.example.boogaloo.models.RecyclerItem
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.catchup_item_layout.view.*
import org.json.JSONObject

class CatchUpAdapter(private val dataset: ArrayList<RecyclerItem>,
                     val listener: (String) -> Unit):
        RecyclerView.Adapter<CatchUpAdapter.ViewHolder>() {

    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
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
//        holder.count.text = dataset.data[position].cloudcastCount
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
        diffResult.dispatchUpdatesTo(this)

    }
}

class CatchUpDiffCallback(
    private val oldDataset: ArrayList<RecyclerItem>,
    private val newDataset: ArrayList<RecyclerItem>
): DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDataset[oldItemPosition].name == newDataset[newItemPosition].name
    }

    override fun getOldListSize(): Int {
        return oldDataset.size
    }

    override fun getNewListSize(): Int {
        return newDataset.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDataset[oldItemPosition] == newDataset[newItemPosition]
    }

}

