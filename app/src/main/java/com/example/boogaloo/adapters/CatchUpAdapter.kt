package com.example.boogaloo.adapters

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.boogaloo.R
import com.example.boogaloo.activities.ShowActivity
import com.example.boogaloo.models.PlaylistResponse
import kotlinx.android.synthetic.main.catchup_item_layout.view.*

class CatchUpAdapter(private val dataset: PlaylistResponse):
        RecyclerView.Adapter<CatchUpAdapter.ViewHolder>() {

//    private lateinit var itemClickListener: OnItemClickListener
//
//    interface OnItemClickListener {
//        fun onItemClick()
//    }

//    fun setOnItemClickListener(listener: OnItemClickListener) {
//        itemClickListener = listener
//    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val name = itemView.tv_show_name
        val thumbnail = itemView.iv_show_thumbnail
        var slug: String? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            //            val itemPosition = adapterPosition
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

//        catchupView.setOnClickListener {view ->
//            onClick(view)
//        }

        return ViewHolder(catchupView)
    }

//    fun onClick(view: View) {
////        val itemPosition
////        Log.d("CUA", )
//    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Log.d("CUA", "position: " + position)

        holder.name.text = dataset.data[position].name
        Glide.with(holder.thumbnail.context)
            .load(dataset.data[0].owner.pictures.extraLarge)
            .into(holder.thumbnail)
        holder.slug = dataset.data[position].slug
    }

    override fun getItemCount(): Int {
        return dataset.data.size
    }

}


