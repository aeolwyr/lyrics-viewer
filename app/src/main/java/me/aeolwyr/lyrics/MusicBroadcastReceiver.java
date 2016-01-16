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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Broadcast receiver which gets signalled when the song change.
 */
public class MusicBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // put the information in the preferences, so that the main activity
        // can access them easily
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("artist", intent.getExtras().getString("artist"));
        editor.putString("track", intent.getExtras().getString("track"));
        editor.apply();
    }
}
