package com.example.musicplayer
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale


class MainActivity : ComponentActivity() {
    private lateinit var playlistButton: Button
    private lateinit var addToPlaylistButton: Button
    private lateinit var clearButton: Button
    private lateinit var pauseButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var musicListView: ListView
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var musicList: ArrayList<Music>
    private lateinit var filteredMusicList: ArrayList<Music>
    private lateinit var currentTrackTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private var mediaPlayer: MediaPlayer? = null
    private var selectedSong: Music? = null

    private var playlists: MutableMap<String, ArrayList<Music>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        loadPlaylistsFromSharedPreferences()
        initializeUIElements()
        initializeMusicListAndAdapter()
    }

    private fun savePlaylistsToSharedPreferences() {
        val editor = sharedPreferences.edit()
        val playlistsJson = convertPlaylistsToJson()
        editor.putString("playlists", playlistsJson)
        editor.apply()
    }

    private fun loadPlaylistsFromSharedPreferences() {
        val playlistsJson = sharedPreferences.getString("playlists", null)
        playlists = if (!playlistsJson.isNullOrBlank()) {
            val gson = Gson()
            val type = object : TypeToken<MutableMap<String, ArrayList<Music>>>() {}.type
            gson.fromJson(playlistsJson, type)
        } else {
            mutableMapOf()
        }
    }

    private fun convertPlaylistsToJson(): String? {
        val gson = Gson()
        val type = object : TypeToken<MutableMap<String, ArrayList<Music>>>() {}.type
        return gson.toJson(playlists, type)
    }

    private fun initializeUIElements () {
        playlistButton = findViewById(R.id.playlistButton);
        addToPlaylistButton = findViewById(R.id.addToPlaylistButton);
        clearButton = findViewById(R.id.clearButton);
        pauseButton = findViewById(R.id.pauseButton);
        searchEditText = findViewById(R.id.searchEditText);
        musicListView = findViewById(R.id.musicListView);
        currentTrackTextView = findViewById(R.id.currentTrackTextView);
    }

    private fun initializeMusicListAndAdapter () {
        musicList = ArrayList()
        initializeMusicList()
        filteredMusicList = ArrayList(musicList)
        musicAdapter = MusicAdapter(this, filteredMusicList)
        musicListView.adapter = musicAdapter
        setItemClickListenerForListView()
        setButtonClickListeners()
        setTextWatcherForSearchEditText()
    }


    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun setItemClickListenerForListView() {
        musicListView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                val selectedSong = filteredMusicList[position]
                showToast("Включён трек ${selectedSong!!.name}")
                playMusic(selectedSong)
            }
        musicListView.onItemLongClickListener = null
    }

    private fun setButtonClickListeners() {
        playlistButton.setOnClickListener {
            viewPlaylists()
        }
        addToPlaylistButton.setOnClickListener {
            addToPlaylist()
        }
        clearButton.setOnClickListener {
            clearMusicSearch()
        }
        pauseButton.setOnClickListener {
            pauseMusic()
        }
    }

    private fun pauseMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                pauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                mediaPlayer!!.start()
                pauseButton.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
    }

    private fun clearMusicSearch() {
        searchEditText.setText("")
        filteredMusicList.clear()
        filteredMusicList.addAll(musicList)
        musicAdapter.notifyDataSetChanged()
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
            if (music!!.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            ) {
                filteredMusicList.add(music)
            }
        }
        musicAdapter.notifyDataSetChanged()
        setItemClickListenerForListView()
    }

    private fun initializeMusicList() {
        musicList.add(Music("Шаман - Мой бой", "shaman_moy_boy"))
        musicList.add(Music("Шаман - Я русский", "shaman_ya_russkiy"))
        musicList.add(Music("Михаил Шафутинский - Душа болит", "mickhail_shufutinskiy_dusha_bolit"))
        musicList.add(Music("Михаил Шафутинский - Третье сентября", "mickhail_shufutinskiy_terie_sentyabrya"))
    }

    @SuppressLint("DiscouragedApi")
    private fun playMusic(music: Music? = null) {
        selectedSong = music ?: filteredMusicList.random()
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }
        val resId = resources.getIdentifier(selectedSong!!.path, "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.setOnCompletionListener {
            playMusic()
        }
        mediaPlayer?.start()
        currentTrackTextView.text = "Сейчас играет: ${selectedSong!!.name}"
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        savePlaylistsToSharedPreferences()
    }

    private fun addToPlaylist() {
        selectedSong?.let { showPlaylistSelectionDialog(it) }
    }

    private fun addToPlaylist(playlistName: String, song: Music) {
        var playlist: ArrayList<Music>? = playlists[playlistName]
        if (playlist == null) {
            playlist = ArrayList()
            playlists[playlistName] = playlist
        }
        playlist.add(song)
        savePlaylistsToSharedPreferences()
    }

    private fun viewPlaylists() {
        val playlistsText = ArrayList<String>(playlists.keys)
        val playlistAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, playlistsText)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Плейлисты")
        builder.setAdapter(playlistAdapter) { _, position ->
            val playlistName = playlistsText[position]
            showMusicFromPlaylist(playlistName)
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showMusicFromPlaylist(playlistName: String) {
        val musicFromPlaylist = playlists[playlistName]
        if (musicFromPlaylist != null) {
            filteredMusicList.clear()
            filteredMusicList.addAll(musicFromPlaylist)
            musicAdapter.notifyDataSetChanged()

            val listView = findViewById<ListView>(R.id.musicListView)
            listView.setOnItemLongClickListener { _, _, position, _ ->
                val selectedTrack = filteredMusicList[position]
                showDeleteTrackDialog(playlistName, selectedTrack)
                true
            }

            showToast("Показана музыка из плейлиста: $playlistName")
        }
    }

    private fun showPlaylistSelectionDialog(selectedSong: Music) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_select_playlist, null)
        builder.setView(dialogView)
        val editTextNewPlaylist = dialogView.findViewById<EditText>(R.id.editTextNewPlaylist)
        val listViewPlaylists = dialogView.findViewById<ListView>(R.id.listViewPlaylists)
        val playlistNames: ArrayList<String?> = ArrayList(playlists.keys)
        val playlistAdapter = PlaylistAdapter(this, playlistNames)
        builder.setPositiveButton(
            "Создать новый плейлист"
        ) { _, _ ->
            val newPlaylistName = editTextNewPlaylist.text.toString().trim { it <= ' ' }
            if (newPlaylistName.isNotEmpty()) {
                addToPlaylist(newPlaylistName, selectedSong)
                showToast("Создан плейлист '$newPlaylistName' с треком '${selectedSong!!.name}'")
            } else {
                showToast("Введите название плейлиста")
            }
        }
        val alertDialog = builder.create()
        listViewPlaylists.adapter = playlistAdapter
        listViewPlaylists.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                val playlistName: String? = playlistAdapter.getItem(position)
                if (playlistName != null) {
                    addToPlaylist(playlistName, selectedSong)
                }
                showToast("Добавлен трек '${selectedSong!!.name}' в плейлист $playlistName")
                alertDialog.dismiss()
            }
        alertDialog.show()
    }

    private fun deleteTrackFromPlaylist(playlistName: String, track: Music) {
        val playlist = playlists[playlistName]
        playlist?.remove(track)
        if (playlist?.isEmpty() == true) {
            playlists.remove(playlistName)
            showToast("Плейлист '$playlistName' удалён, так как в нём не осталось треков.")
            clearMusicSearch()
        } else {
            showToast("Удалён трек '${track.name}' из плейлиста $playlistName")
        }
        savePlaylistsToSharedPreferences()
    }

    private fun showDeleteTrackDialog(playlistName: String, track: Music) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Удаление трека")
        builder.setMessage("Вы действительно хотите удалить трек '${track.name}' из плейлиста '$playlistName'?")
        builder.setPositiveButton("Да") { _, _ ->
            deleteTrackFromPlaylist(playlistName, track)
            showMusicFromPlaylist(playlistName)
        }
        builder.setNegativeButton("Отмена") { _, _ -> }
        builder.show()
    }

}
