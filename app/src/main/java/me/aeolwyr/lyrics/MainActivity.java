/*
 * Lyrics Viewer is an app to view the lyrics embedded inside the music files.
 * Copyright (c) 2006 Kaan Karaagacli
 *
 * This file is part of ID3 Lyrics.
 *
 * Lyrics Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lyrics Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Lyrics Viewer.  If not, see <http://www.gnu.org/licenses/>
 */

package me.aeolwyr.lyrics;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * The activity has shows the lyrics. It also listens for any lyrics change while it is active.
 */
public class MainActivity extends Activity {
    private TextView lyricsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lyricsView = (TextView) findViewById(R.id.lyrics);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLyrics();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    /** this is called when the lyrics change **/
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            showLyrics();
        }
    };

    /**
     * Fetch the lyrics and show them on the main text view.
     */
    private void showLyrics() {
        // MusicBroadcastReceiver stores the artist/track information in the preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String artist = sharedPrefs.getString("artist", "");
        String track = sharedPrefs.getString("track", "");

        // update the action bar if possible
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (!track.isEmpty()) {
                getActionBar().setTitle(track);
            } else {
                getActionBar().setTitle(R.string.app_name);
            }
            if (!artist.isEmpty()) {
                getActionBar().setSubtitle(artist);
            } else {
                getActionBar().setSubtitle(null);
            }
        }

        // find the audio file
        Uri uri = Uri.parse("content://media/external/audio/media");
        String[] projection = new String[]{"artist", "title", "_data"};
        String selection = "artist=? AND title=?";
        String[] selectionArgs = new String[]{artist, track};

        // ask for the read permission on marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(READ_EXTERNAL_STORAGE,
                    android.os.Process.myPid(), android.os.Process.myUid())
                    != PackageManager.PERMISSION_GRANTED) {
                // permission needed
                if (!shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                    // asking for the first time
                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 0);
                } else {
                    // denied previously
                    lyricsView.setText(R.string.permission_denied);
                }
                // don't continue without the permission
                return;
            }
        }

        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);

        String lyrics = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // audio file is found
                String path = cursor.getString(2);
                try {
                    // extract the tag
                    AudioFile audioFile = AudioFileIO.read(new File(path));
                    lyrics = audioFile.getTag().getFirst(FieldKey.LYRICS);
                } catch (CannotReadException | IOException | TagException
                        | ReadOnlyFileException | InvalidAudioFrameException ignored) {
                    // pass
                }
            }
            cursor.close();
        }
        if (lyrics != null && !lyrics.isEmpty()) {
            // lyrics was found
            lyricsView.setText(lyrics);
        } else {
            // no file found, no lyrics found, or some error has occurred
            lyricsView.setText(R.string.no_lyrics);
        }
    }

}
