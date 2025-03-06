package com.dcelysia.outsourceserviceproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.R

class ModelsAdapter(
    private val voiceItems: List<VoiceItem>,
    private val choice: (String) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TEXT = 0
        private const val VIEW_TYPE_MODEL = 1
    }

    inner class ModelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val btnPlayPause: ImageView = itemView.findViewById(R.id.btn_play_pause)
        val playImageView: ImageView = itemView.findViewById(R.id.view_audio_progress)
        val allModel: CardView = itemView.findViewById(R.id.all_model)
        fun bind(voiceItem: VoiceItem) {

            allModel.setOnClickListener {
                choice(voiceItem.title)
            }
            ivAvatar.setImageResource(voiceItem.avatarResId)
            tvTitle.text = voiceItem.title
            tvDescription.text = voiceItem.description
            tvDuration.text = voiceItem.duration

            // Update play/pause button based on state
            if (voiceItem.isPlaying) {
                btnPlayPause.setImageResource(R.drawable.pause)
                btnPlayPause.setBackgroundResource(R.drawable.pause)
                playImageView.setImageResource(R.drawable.yinbo_blue)
            } else {
                btnPlayPause.setImageResource(R.drawable.play)
                btnPlayPause.setBackgroundResource(R.drawable.play)
                playImageView.setImageResource(R.drawable.yinbo_grey)
            }
        }
    }

    inner class TextViewModel(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_title)

        fun bind(text: String) {

            title.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                TextViewModel(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.recycler_text_title, parent, false)
                )
            }

            else -> {
                ModelViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_recommended_voice, parent, false)
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 || position == 5) {
            VIEW_TYPE_TEXT
        } else {
            VIEW_TYPE_MODEL
        }
    }

    override fun getItemCount() = voiceItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextViewModel -> {
                if (position == 0) {
                    holder.bind("推荐")
                } else {
                    holder.bind("我的")
                }
            }

            is ModelViewHolder -> {
                if (position < voiceItems.size) {
                    holder.bind(voiceItems[position - 1])
                } else {
                    holder.bind(voiceItems[position - 2])
                }
            }
        }
    }
}