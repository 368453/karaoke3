/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.karaoke

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.karaoke.data.Song
import com.example.karaoke.data.songs
import com.example.karaoke.ui.theme.KaraokeTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaraokeTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        KaraokeTopAppBar()
                    }
                ) {
                    KaraokeApp()
                }
            }
        }
    }
}

/**
 * Composable that displays an app bar and a list of songs.
 */
@Composable
fun KaraokeApp() {
    LazyColumn {
        items(songs) {
            SongItem(
                song = it,
                modifier = Modifier)
            )
        }
    }
}

/**
 * Composable that displays a list item containing a song title and artist information.
 *
 * @param song contains the data that populates the list item
 * @param modifier modifiers to set to this composable
 */
@Composable
fun SongItem(
    song: Song,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SongInformation(song.title, song.artist)
        }
    }
}

/**
 * Composable that displays a Top Bar with an icon and text.
 *
 * @param modifier modifiers to set to this composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaraokeTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "R.string.app_name",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Composable that displays song information (title and artist).
 *
 * @param songTitle is the resource ID for the string of the song title
 * @param artistName is the resource ID for the string of the artist name
 * @param modifier modifiers to set to this composable
 */
@Composable
fun SongInformation(
    @StringRes songTitle: Int,
    @StringRes artistName: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(songTitle),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
        )
        Text(
            text = stringResource(artistName),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Composable that displays what the UI of the app looks like in light theme in the design tab.
 */
@Preview
@Composable
fun KaraokePreview() {
    KaraokeTheme(darkTheme = false) {
        KaraokeApp()
    }
}

@Composable
fun KaraokeTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}

/**
 * Composable that displays what the UI of the app looks like in dark theme in the design tab.
 */
@Preview
@Composable
fun KaraokeDarkThemePreview() {
    KaraokeTheme(darkTheme = true) {
        KaraokeApp()
    }
}
