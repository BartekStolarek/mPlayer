package stolarek.bartosz.mplayer2;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class LoadSongs extends AsyncTask<Void, Void, Void> {
    ContentResolver contentResolver;
    Context mContext;

    public LoadSongs(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        loadSongs();
        return null;
    }
    public void loadSongs() {
        contentResolver = mContext.getContentResolver();

        //get songs from SD
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //default stuff
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        //read access to the result returned by db query
        Cursor songCursor = contentResolver.query(songUri, projection, selection, null, null);

        if(songCursor != null && songCursor.moveToFirst())
        {
            //append songs into arrayList
            do
            {
                MainActivity.songsList.add(new Song(songCursor.getString(0), songCursor.getString(1), songCursor.getString(2)));

            } while(songCursor.moveToNext());
            songCursor.close();
        }
    }
}
