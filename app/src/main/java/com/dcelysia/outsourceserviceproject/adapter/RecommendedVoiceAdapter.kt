package com.dcelysia.outsourceserviceproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dcelysia.outsourceserviceproject.Model.data.response.VoiceItem
import com.dcelysia.outsourceserviceproject.R

class RecommendedVoiceAdapter(
    private val voiceItems: List<VoiceItem>,
    private val onPlayPauseClick: (Int) -> Unit
) : RecyclerView.Adapter<RecommendedVoiceAdapter.VoiceViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommended_voice, parent, false)
        return VoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoiceViewHolder, position: Int) {
        val voiceItem = voiceItems[position]
        holder.bind(voiceItem)

        holder.btnPlayPause.setOnClickListener {
            onPlayPauseClick(position)
        }

    }

    override fun getItemCount(): Int = voiceItems.size

    fun updatePlayState(position: Int) {
        notifyItemChanged(position)
    }

    inner class VoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val btnPlayPause: ImageView = itemView.findViewById(R.id.btn_play_pause)
        val playImageView: ImageView = itemView.findViewById(R.id.view_audio_progress)
        fun bind(voiceItem: VoiceItem) {
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
}