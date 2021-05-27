package com.arcmce.boogaloo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.models.CatchupRecyclerItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.catchup_item_layout.view.*


class CatchUpAdapter(private var dataset: ArrayList<CatchupRecyclerItem>,
                     val cloudcastListener: (String) -> Unit,
                     private val listener: (CatchupRecyclerItem) -> Unit):
        androidx.recyclerview.widget.RecyclerView.Adapter<CatchUpAdapter.ViewHolder>() {

    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

//    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val name = itemView.tv_show_name
//        val count = itemView.tv_cloudcast_count
        val thumbnail = itemView.iv_show_thumbnail
        var slug: String? = null
        var cloudcastUrlList: ArrayList<String>? = null

//        val context = itemView.context
//        val showCatchupShowIntent = Intent(context, ShowActivity::class.java)

//        init {
//            itemView.setOnClickListener(this)
//        }

//        override fun onClick(v: View) {
//            val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
//            val transaction = manager.beginTransaction()
//            transaction.replace(R.id.tab_fragment, CloudcastFragment())
////            transaction.disallowAddToBackStack()
//            transaction.commit()
//
//            Log.d("CUA", "clicked item position: " + adapterPosition.toString())
//            showCatchupShowIntent.putExtra("SLUG", slug)
//            showCatchupShowIntent.putExtra("CLOUDCAST", cloudcastUrlList)
//            context.startActivity(showCatchupShowIntent)
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val catchupView = LayoutInflater.from(parent.context)
           .inflate(R.layout.catchup_item_layout, parent, false)

        return ViewHolder(catchupView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]

        Log.d("CUA", "onBindViewHolder " + item.slug)

        holder.name.text = item.name
        Glide.with(holder.thumbnail.context)
            .load(item.thumbnail)
            .apply(requestOptions)
            .into(holder.thumbnail)
        holder.slug = item.slug
        holder.cloudcastUrlList = null

        cloudcastListener(item.slug)

        holder.itemView.setOnClickListener { listener(item) }

    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun updateList(newList: ArrayList<CatchupRecyclerItem>) {
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
            holder.cloudcastUrlList = dataset[position].url

            Log.d("CUA", "onBindViewHolderUpdate")

        }
    }
}

class CatchUpDiffCallback(
    private val oldDataset: ArrayList<CatchupRecyclerItem>,
    private val newDataset: ArrayList<CatchupRecyclerItem>
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

