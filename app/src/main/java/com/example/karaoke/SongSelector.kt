package com.example.karaoke

import ApiResponse
import GetCharts
import SearchArtist
import SearchSong
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.karaoke.db.database.LyricDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Entry(
    val track_id: Int,
    val track_name: String,
    val track_artist: String
) {
    override fun toString(): String
    {
        return "$track_artist - $track_name"
    }
}


class SongSelector : AppCompatActivity()
{
    private lateinit var db : LyricDatabase

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.song_selector)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val category = intent.getLongExtra("category", 0)
        val query = intent.getStringExtra("query").toString()
        val header = intent.getStringExtra("header").toString()

        title = header

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.musixmatch.com/ws/1.1/") // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var call: Call<ApiResponse>

        // top 100
        if (category == 1.toLong())
        {
            val apiService = retrofit.create(GetCharts::class.java)
            call = apiService.getData(
                chart_name = "top",
                page = 1,
                page_size = 100,
                has_lyrics = 1,
                apikey = "8d4f8325913c450ca4acdd45bf035687"
            )
        }

        // search artist
        else if (category == 2.toLong())
        {
            val apiService = retrofit.create(SearchArtist::class.java)
            call = apiService.getData(
                q_artist = query,
                page = 1,
                page_size = 100,
                has_lyrics = 1,
                apikey = "8d4f8325913c450ca4acdd45bf035687"
            )
        }

        // search song
        else
        {
            val apiService = retrofit.create(SearchSong::class.java)
            call = apiService.getData(
                q_artist = query,
                page = 1,
                page_size = 100,
                has_lyrics = 1,
                apikey = "8d4f8325913c450ca4acdd45bf035687"
            )
        }

        // list to put the songs in
        val choices = mutableListOf<Entry>()

        // not downloaded
        if (category > 0.toLong()) {

            // Execute the request asynchronously
            call.enqueue(object : retrofit2.Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: retrofit2.Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        data?.let {
                            it.message.body.track_list.forEach { track ->

                                choices.add(
                                    Entry(
                                        track_id = track.track.track_id,
                                        track_name = track.track.track_name,
                                        track_artist = track.track.artist_name
                                    )
                                )
                            }
                            runOnUiThread { updateUI(choices) }
                        }
                    }
                }

                // if it realllyyyyy doesn't work
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("api", "API Call failed with error: ${t.message}")
                }
            })
        }

        // track is downloaded
        else
        {

            db = Room.databaseBuilder(applicationContext, LyricDatabase::class.java, "lyric_database").fallbackToDestructiveMigration().build()

            lifecycleScope.launch(Dispatchers.IO) {
                db.lyricDao().getAllItems().collect { retrievedLyric ->
                    for (item in retrievedLyric) {
                        choices.add(
                            Entry(
                                track_id = item.id,
                                track_name = item.songName,
                                track_artist = item.songArtist
                            )
                        )
                    }
                    runOnUiThread { updateUI(choices) }
                }
            }

        }
    }

    private fun updateUI(choices: List<Entry>) {
        // Extract song names from choices list
        val songNames = choices.mapIndexed { index, entry ->
            "${index + 1}. ${entry.toString()}"
        }

        // homescreen choices
        val arrayAdapter: ArrayAdapter<*>

        // access the listView from xml file
        val mListView = findViewById<ListView>(R.id.userlist)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, songNames)
        mListView.adapter = arrayAdapter

        // when an item in the list is clicked
        mListView.setOnItemClickListener { parent, view, position, id ->
            // create a new screen displaying the song and lyrics
            val intent = Intent(this, LyricsActivity::class.java)
            intent.putExtra("track_id", choices[position].track_id)
            intent.putExtra("track_name", choices[position].track_name)
            intent.putExtra("artist_name", choices[position].track_artist)

            // show the screen!
            startActivity(intent)
        }
    }
}
