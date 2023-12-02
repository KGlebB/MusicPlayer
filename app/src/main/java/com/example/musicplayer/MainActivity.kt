package com.example.musicplayer
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
    private lateinit var addToPlaylistButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var musicListView: ListView
    private lateinit var musicAdapter: ArrayAdapter<String>
    private lateinit var musicList: ArrayList<String>
    private lateinit var filteredMusicList: ArrayList<String>

    private var mediaPlayer: MediaPlayer? = null
    private var selectedSong: String? = null;

    private val playlists: MutableMap<String, ArrayList<String>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        initializeUIElements()
        initializeMusicListAndAdapter()
    }

    private fun initializeUIElements () {
        playerButton = findViewById(R.id.playerButton);
        playlistButton = findViewById(R.id.playlistButton);
        addToPlaylistButton = findViewById(R.id.addToPlaylistButton);
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
            viewPlaylists()
        }
        addToPlaylistButton.setOnClickListener {
            addToPlaylist()
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
        selectedSong = fileName
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }
        val resId = resources.getIdentifier(selectedSong, "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    private fun addToPlaylist() {
        selectedSong?.let { showPlaylistSelectionDialog(it) }
    }

    private fun addToPlaylist(playlistName: String, song: String) {
        var playlist: ArrayList<String>? = playlists[playlistName]
        if (playlist == null) {
            playlist = ArrayList()
            playlists?.put(playlistName, playlist)
        }
        playlist.add(song)
    }

    private fun viewPlaylists() {
        val playlistsText = StringBuilder("Playlists:\n")
        for (playlistName in playlists.keys) {
            playlistsText.append("- ").append(playlistName).append("\n")
        }
        Toast.makeText(this, playlistsText.toString(), Toast.LENGTH_LONG).show()
    }

    private fun showPlaylistSelectionDialog(selectedSong: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_select_playlist, null)
        builder.setView(dialogView)
        val editTextNewPlaylist = dialogView.findViewById<EditText>(R.id.editTextNewPlaylist)
        val listViewPlaylists = dialogView.findViewById<ListView>(R.id.listViewPlaylists)
        val playlistNames: ArrayList<String?> = ArrayList(playlists.keys)
        val playlistAdapter = PlaylistAdapter(this, playlistNames)
        listViewPlaylists.adapter = playlistAdapter
        val alertDialog = builder.create()
        listViewPlaylists.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val playlistName: String? = playlistAdapter.getItem(position)
                if (playlistName != null) {
                    addToPlaylist(playlistName, selectedSong)
                }
                Toast.makeText(
                    this@MainActivity,
                    "Added '$selectedSong' to $playlistName",
                    Toast.LENGTH_SHORT
                ).show()
                alertDialog.dismiss()
            }
        builder.setPositiveButton(
            "Create Playlist"
        ) { _, _ ->
            val newPlaylistName = editTextNewPlaylist.text.toString().trim { it <= ' ' }
            if (!newPlaylistName.isEmpty()) {
                addToPlaylist(newPlaylistName, selectedSong)
                Toast.makeText(
                    this@MainActivity,
                    "Created playlist '$newPlaylistName' and added '$selectedSong'",
                    Toast.LENGTH_SHORT
                ).show()
                alertDialog.dismiss()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please enter a playlist name",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
        alertDialog.show()
    }
}
