package com.example.karaoke

import ApiResponse
import GetLyrics
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.karaoke.db.database.Lyric
import com.example.karaoke.db.database.LyricDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LyricsActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_lyrics)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val lyricsTextView = findViewById<TextView>(R.id.lyricsTextView)
        val trackId = intent.getIntExtra("track_id", 0)
        val trackName = intent.getStringExtra("track_name").toString()
        val artistName = intent.getStringExtra("artist_name").toString()

        title = trackName

        // build the database
        db = Room.databaseBuilder(applicationContext, LyricDatabase::class.java, "lyric_database").fallbackToDestructiveMigration().build()

        lifecycleScope.launch(Dispatchers.IO) {
            // Insert the lyric
            db.lyricDao().getItem(trackId).collect { retrievedLyric ->

                if (retrievedLyric != null)
                {
                    Log.i("AAA","loading from database")
                    runOnUiThread { updateUI(retrievedLyric.songLyric, retrievedLyric.id, retrievedLyric.songName, retrievedLyric.songArtist, lyricsTextView, true) }
                }

                else
                {
                    Log.i("AAA","loading from api")
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.musixmatch.com/ws/1.1/") // Replace with your API base URL
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    // Create an instance of your API interface
                    val apiService = retrofit.create(GetLyrics::class.java)

                    // top 100
                    val call = apiService.getData(
                        track_id = trackId,
                        apikey = "8d4f8325913c450ca4acdd45bf035687"
                    )

                    // Execute the request asynchronously
                    call.enqueue(object : retrofit2.Callback<ApiResponse> {
                        override fun onResponse(
                            call: Call<ApiResponse>,
                            response: retrofit2.Response<ApiResponse>
                        ) {
                            if (response.isSuccessful) {
                                val data = response.body()
                                data?.let {
                                    runOnUiThread { updateUI(it.message.body.lyrics.lyrics_body, trackId, trackName, artistName, lyricsTextView, false) }
                                }
                            }
                        }

                        // if it really doesn't work
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Log.e("api", "API Call failed with error: ${t.message}")
                        }
                    })

                }
            }

        }
    }


    private fun updateUI(lyrics: String, trackId: Int, trackName: String, artistName: String, textView: TextView, local: Boolean)
    {
        val downloadButton: Button = findViewById(R.id.downloadButton)

        if (local) downloadButton.text = "Delete"

        textView.text = lyrics

        downloadButton.setOnClickListener {

            if (!local)
            {
                lifecycleScope.launch(Dispatchers.IO) {
                    // Insert the lyric
                    db.lyricDao().insert(
                        Lyric(
                            id = trackId,
                            songName = trackName,
                            songArtist = artistName,
                            songLyric = lyrics
                        )
                    )
                }
            }

            else
            {
                lifecycleScope.launch(Dispatchers.IO) {
                    // Insert the lyric
                    db.lyricDao().removeItem(trackId)
                }
            }

            finish()

        }
    }
}