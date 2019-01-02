package stolarek.bartosz.mplayer2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


//connection between list of songs and ListView
public class Songs extends ArrayAdapter<Song> {
    Intent musicService;
    private final Activity context;
    List<Song> song = new ArrayList<Song>();

    public Songs(Activity context, List<Song> song) {
        super(context, R.layout.songs_layout, song);
        this.context = context;
        this.song = song;

        musicService = new Intent(context, MusicService.class);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map the title and artists to the TextView
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.songs_layout, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.textName);
        TextView txtArtist = (TextView) rowView.findViewById(R.id.textArtist);
        ImageView albumImage = (ImageView) rowView.findViewById(R.id.albumImage);
        pl.droidsonroids.gif.GifImageView equalizerGif = (pl.droidsonroids.gif.GifImageView) rowView.findViewById(R.id.equalizerGif);

        txtTitle.setText(song.get(position).getName());
        txtArtist.setText(song.get(position).getArtist());

        if (MusicService.actualPlayingPosition != null) {
            if (position == MusicService.actualPlayingPosition) {
                txtTitle.setTextColor(context.getResources().getColor(R.color.playingColor));

                if (MusicService.mediaPlayer.isPlaying()) {
                    albumImage.setVisibility(View.GONE);
                    equalizerGif.setVisibility(View.VISIBLE);
                }
            }
        }

        return rowView;
    }
}
