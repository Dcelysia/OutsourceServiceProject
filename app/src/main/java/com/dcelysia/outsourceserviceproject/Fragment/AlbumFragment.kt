package com.dcelysia.outsourceserviceproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dcelysia.outsourceserviceproject.R
import com.dcelysia.outsourceserviceproject.databinding.FragmentAlbumBinding
import com.dcelysia.outsourceserviceproject.databinding.ItemSongBinding
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

class AlbumFragment : Fragment() {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!

    private val songAdapter = SongAdapter()
    private var album: Album? = null

    companion object {
        private const val ARG_ALBUM_ID = "album_id"

        fun newInstance(albumId: String): AlbumFragment {
            val fragment = AlbumFragment()
            val args = Bundle()
            args.putString(ARG_ALBUM_ID, albumId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupToolbar()
        setupListeners()

        // 获取专辑ID并加载专辑数据
        val albumId = arguments?.getString(ARG_ALBUM_ID)
        if (albumId != null) {
            loadAlbumData(albumId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        // 设置RecyclerView
        binding.rvSongList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }
    }

    private fun setupToolbar() {
        // 设置标题透明度随滚动变化
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val scrollRange = appBarLayout.totalScrollRange
            val offsetRatio = abs(verticalOffset).toFloat() / scrollRange.toFloat()

            // 当折叠时显示标题，展开时隐藏标题
            if (offsetRatio > 0.5) {
                binding.collapsingToolbar.title = album?.name ?: "专辑详情"
            } else {
                binding.collapsingToolbar.title = " "
            }
        })
    }

    private fun setupListeners() {
        // 播放全部按钮点击事件
        binding.btnPlayAll.setOnClickListener {
            Toast.makeText(context, "播放全部", Toast.LENGTH_SHORT).show()
        }

        // 收藏按钮点击事件
        binding.btnFavorite.setOnClickListener {
            Toast.makeText(context, "收藏", Toast.LENGTH_SHORT).show()
        }

        // 评论按钮点击事件
        binding.btnComment.setOnClickListener {
            Toast.makeText(context, "评论", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadAlbumData(albumId: String) {
        // 这里应该从Repository或ViewModel获取数据
        // 这里仅为演示，使用模拟数据
        album = Album(
            id = albumId,
            name = "夜曲",
            artist = "周杰伦",
            coverUrl = "https://example.com/album_cover.jpg",
            description = "《夜曲》是周杰伦2006年发行的专辑《依然范特西》中的一首歌曲，由周杰伦作曲，黄俊郎作词。",
            songs = getMockSongs()
        )

        // 更新UI
        updateUI()
    }

    private fun updateUI() {
        album?.let { album ->
            binding.tvAlbumName.text = album.name
            binding.tvArtistName.text = album.artist
            binding.tvAlbumDesc.text = album.description

            // 加载专辑封面
            Glide.with(this)
                .load(album.coverUrl)
                .placeholder(R.drawable.album_time)
                .error(R.drawable.album_time)
                .into(binding.ivAlbumCover)

            // 加载背景图片（模糊效果在XML中通过alpha和overlay实现）
            Glide.with(this)
                .load(album.coverUrl)
                .placeholder(R.drawable.album_time)
                .error(R.drawable.album_time)
                .into(binding.ivAlbumBackground)

            // 更新歌曲列表
            songAdapter.setSongs(album.songs)
        }
    }

    private fun getMockSongs(): List<Song> {
        // 模拟数据，实际应从网络/数据库获取
        return listOf(
            Song("1", "夜曲", "周杰伦", "依然范特西", true, true),
            Song("2", "发如雪", "周杰伦", "十一月的萧邦", true, false),
            Song("3", "黑色毛衣", "周杰伦", "十一月的萧邦", false, false),
            Song("4", "四面楚歌", "周杰伦", "依然范特西", true, false),
            Song("5", "枫", "周杰伦", "十一月的萧邦", false, true),
            Song("6", "浪漫手机", "周杰伦", "依然范特西", true, false),
            Song("7", "蓝色风暴", "周杰伦", "依然范特西", false, false),
            Song("8", "逆鳞", "周杰伦", "依然范特西", true, false),
            Song("9", "麦芽糖", "周杰伦", "依然范特西", false, false),
            Song("10", "退后", "周杰伦", "依然范特西", true, true)
        )
    }

    // 歌曲列表适配器
    inner class SongAdapter : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

        private var songs: List<Song> = emptyList()

        fun setSongs(songs: List<Song>) {
            this.songs = songs
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
            val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SongViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            val song = songs[position]
            holder.bind(song, position + 1)
        }

        override fun getItemCount(): Int = songs.size

        inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(song: Song, index: Int) {
                binding.tvSongIndex.text = index.toString()
                binding.tvSongName.text = song.name
                binding.tvArtistAlbum.text = "${song.artist} - ${song.album}"

                // 显示或隐藏高品质音频标签
                binding.tvQuality.visibility = if (song.isHighQuality) View.VISIBLE else View.GONE

                // 设置点击事件
                binding.root.setOnClickListener {
                    Toast.makeText(context, "播放: ${song.name}", Toast.LENGTH_SHORT).show()
                }

                binding.ivMoreOptions.setOnClickListener {
                    Toast.makeText(context, "更多选项: ${song.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val coverUrl: String,
    val description: String,
    val songs: List<Song>
)

data class Song(
    val id: String,
    val name: String,
    val artist: String,
    val album: String,
    val isHighQuality: Boolean = false,
    val hasMV: Boolean = false
)