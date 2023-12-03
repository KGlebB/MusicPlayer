package com.example.musicplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.musicplayer.Music

class MusicAdapter(context: Context, musicList: List<Music>) :
    ArrayAdapter<Music>(context, android.R.layout.simple_list_item_1, musicList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val music = getItem(position)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        view.text = music?.name
        return view
    }
}
