package com.example.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.util.Locale


class MainActivity : ComponentActivity() {
    private lateinit var playerButton: Button
    private lateinit var playlistButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var musicListView: ListView
    private lateinit var musicAdapter: ArrayAdapter<String>
    private lateinit var musicList: ArrayList<String>
    private lateinit var filteredMusicList: ArrayList<String>

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        initializeUIElements()
        initializeMusicListAndAdapter()
    }

    private fun initializeUIElements () {
        playerButton = findViewById(R.id.playerButton);
        playlistButton = findViewById(R.id.playlistButton);
        searchEditText = findViewById(R.id.searchEditText);
        musicListView = findViewById(R.id.musicListView);
    }

    private fun initializeMusicListAndAdapter () {
        musicList = ArrayList()
        initializeMusicList()
        filteredMusicList = ArrayList(musicList)
        musicAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filteredMusicList)
        musicListView.adapter = musicAdapter
        setItemClickListenerForListView()
        setButtonClickListeners()
        setTextWatcherForSearchEditText()
    }

    private fun setItemClickListenerForListView() {
        musicListView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                val selectedSong = filteredMusicList[position]
                Toast.makeText(this@MainActivity, "Playing: $selectedSong", Toast.LENGTH_SHORT)
                    .show()
                playMusic(selectedSong);
            }
    }

    private fun setButtonClickListeners() {
        playerButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Player button clicked", Toast.LENGTH_SHORT).show()
        }
        playlistButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Playlist button clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setTextWatcherForSearchEditText() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {  }
            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterMusicList(charSequence.toString())
            }
            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun filterMusicList(query: String) {
        filteredMusicList.clear()
        for (music in musicList) {
            if (music.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            ) {
                filteredMusicList.add(music)
            }
        }
        musicAdapter.notifyDataSetChanged()
    }

    private fun initializeMusicList() {
        musicList.add("shaman_moy_boy")
        musicList.add("shaman_ya_russkiy")
        musicList.add("mickhail_shufutinskiy_dusha_bolit")
        musicList.add("mickhail_shufutinskiy_terie_sentyabrya")
    }

    private fun playMusic(fileName: String) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }
        val resId = resources.getIdentifier(fileName, "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
