package com.dcelysia.outsourceserviceproject.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dcelysia.outsourceserviceproject.Model.Room.entity.AudioDetailEntity
import com.dcelysia.outsourceserviceproject.R

class AudioAdapter(val date: List<AudioDetailEntity>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("ClickableViewAccessibility")
    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val audioPicture: ImageView = view.findViewById(R.id.audio_picture)
        val audioName: TextView = view.findViewById(R.id.audio_name)
        val audioContext: TextView = view.findViewById(R.id.audio_context)
        val saveTime: TextView =view.findViewById(R.id.save_time)
        val scrollView: ScrollView = view.findViewById(R.id.scroll_view)
        val player: ImageView = view.findViewById(R.id.audio_player)
        init {
            scrollView.setOnTouchListener { view, event ->
                // 当用户触摸ScrollView时，告诉父视图不要拦截触摸事件
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 按下时立即请求父视图不要拦截
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 滚动时继续请求父视图不要拦截
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // 抬起或取消时，恢复父视图的拦截能力
                        view.parent.requestDisallowInterceptTouchEvent(false)
                        false
                    }
                    else -> false
                }
            }
        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_detail, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val audioHolder = holder as AudioViewHolder
        audioHolder.audioName.text = date[position].title
        audioHolder.audioContext.text = date[position].content
        audioHolder.saveTime.text = date[position].timestamp
    }

    override fun getItemCount() = date.size


}