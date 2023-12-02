package com.example.musicplayer

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class PlaylistAdapter(context: Context?, playlists: ArrayList<String?>) :
    ArrayAdapter<String?>(context!!, 0, playlists!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val playlistName = getItem(position)
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, parent, false)
        }
        val textViewPlaylistName = convertView!!.findViewById<TextView>(R.id.text1)
        textViewPlaylistName.text = playlistName
        return convertView
    }
}