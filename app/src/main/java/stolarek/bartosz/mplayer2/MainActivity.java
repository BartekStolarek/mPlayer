package stolarek.bartosz.mplayer2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST = 1;
    Intent musicService, musicPlayer;
    private ListView lv;
    public static ArrayList<Song> songsList = new ArrayList<>();
    LoadSongs loadSongs;
    TextView playingTitle, playingArtist, textTitle;
    ImageView previousSong, nextSong, startMusic, albumImage;
    Songs adapter;
    Integer retrieveSong, paused, started;
    View view;
    pl.droidsonroids.gif.GifImageView equalizerGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playingArtist = (TextView) findViewById(R.id.playingArtist);
        playingTitle = (TextView) findViewById(R.id.playingTitle);
        previousSong = (ImageView) findViewById(R.id.previousSong);
        nextSong = (ImageView) findViewById(R.id.nextSong);
        startMusic = (ImageView) findViewById(R.id.startMusic);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }   else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }
        loadSongs = new LoadSongs(this);
        loadSongs.execute();
        hideElements();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("retrieveSong"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("paused"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("started"));
    }

    @Override
    protected void onResume() {
        musicService.putExtra("retrieveSong", true);
        startService(musicService);
        musicService.removeExtra("retrieveSong");
        hideElements();

        super.onResume();
    }

    @Override
    protected void onStart() {
        permissionGranted();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        stopService(musicService);
        super.onDestroy();
    }

    public void permissionGranted() {
        lv = (ListView)findViewById(R.id.ListView);

        adapter = new Songs(MainActivity.this, songsList);
        lv.setAdapter(adapter);
        musicService = new Intent(this, MusicService.class);
        musicPlayer = new Intent(MainActivity.this, PlayerActivity.class);

        //lv.eventListener...
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicPlayer.putExtra("SongPosition", position);
                startActivity(musicPlayer);
                musicPlayer.removeExtra("SongPosition");
            }
        });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            retrieveSong = intent.getIntExtra("retrieveSong", -1);
            paused = intent.getIntExtra("paused", -1);
            started = intent.getIntExtra("started", -1);

            if (retrieveSong >= 0) {
                playingArtist.setText(MainActivity.songsList.get(retrieveSong).getArtist());
                playingTitle.setText(MainActivity.songsList.get(retrieveSong).getName());

                //we are updating list with parameter "stay" when musicService sent "retrieveSong", because we have to handle
                //a situation, where song in musicService has ended and musicService has loaded next song- and MusicService.actualPlayingPosition
                //is now an ACTUAL position of playing
                updateList(MusicService.actualPlayingPosition, "stay");
            }
            if (paused > 0) {
                setPause(true);
            }
            if (started > 0) {
                setPause(false);
            }
        }
    };

    public void playMusic (View v) {
        if (MusicService.playing) {
            musicService.putExtra("MusicService.data", "pause");
            startService(musicService);
            startMusic.setImageResource(R.drawable.play);
        }
        else {
            musicService.putExtra("MusicService.data", "start");
            startService(musicService);
            startMusic.setImageResource(R.drawable.pause);
        }
    }

    public void playNext (View v) {
        updateList(MusicService.actualPlayingPosition, "next");

        musicService.putExtra("checkNextSong", true);
        startService(musicService);
        musicService.removeExtra("checkNextSong");
    }

    public void playPrevious (View v) {
        updateList(MusicService.actualPlayingPosition, "previous");

        musicService.putExtra("checkPreviousSong", true);
        startService(musicService);
        musicService.removeExtra("checkPreviousSong");
    }

    public void openPlayingActivity (View v) {
        startActivity(musicPlayer);
    }

    private void hideElements() {
        if (MusicService.mediaPlayer == null) {
            playingArtist.setVisibility(View.INVISIBLE);
            playingTitle.setVisibility(View.INVISIBLE);
            previousSong.setVisibility(View.INVISIBLE);
            nextSong.setVisibility(View.INVISIBLE);
            startMusic.setVisibility(View.INVISIBLE);
        }
        else {
            playingArtist.setVisibility(View.VISIBLE);
            playingTitle.setVisibility(View.VISIBLE);
            previousSong.setVisibility(View.VISIBLE);
            nextSong.setVisibility(View.VISIBLE);
            startMusic.setVisibility(View.VISIBLE);

            if (MusicService.mediaPlayer.isPlaying()) {
                startMusic.setImageResource(R.drawable.pause);
            }
            else {
                startMusic.setImageResource(R.drawable.play);
            }
        }
    }

    private void updateList(int index, String goTo) {
        //first restore color and image of old-playing

        //if goTo == stay means, that song in musicService has ended and musicService loaded automatically next song, so we have to clear previous song
        if (goTo.equals("stay")) {
            view = lv.getChildAt(index - 1 - lv.getFirstVisiblePosition());
            if (view == null)
                view = lv.getChildAt(lv.getLastVisiblePosition() - lv.getFirstVisiblePosition());
        }
        else {
            view = lv.getChildAt(index - lv.getFirstVisiblePosition());
            if (view == null)
                return;
        }

        textTitle = (TextView) view.findViewById(R.id.textName);
        albumImage = (ImageView) view.findViewById(R.id.albumImage);
        equalizerGif = (pl.droidsonroids.gif.GifImageView) view.findViewById(R.id.equalizerGif);

        textTitle.setTextColor(this.getResources().getColor(R.color.white));
        equalizerGif.setVisibility(View.GONE);
        albumImage.setVisibility(View.VISIBLE);

        //then apply new color to actual playing position
        if (goTo.equals("previous")) {
            view = lv.getChildAt(index - 1 - lv.getFirstVisiblePosition());

            if (view == null)
                view = view = lv.getChildAt(lv.getLastVisiblePosition() - lv.getFirstVisiblePosition());
        }

        else if (goTo.equals("next")) {
            view = lv.getChildAt(index + 1 - lv.getFirstVisiblePosition());

            if (view == null)
                view = view = lv.getChildAt(0 - lv.getFirstVisiblePosition());
        }
        else if (goTo.equals("stay")) {
            view = lv.getChildAt(index - lv.getFirstVisiblePosition());

            if (view == null)
                return;
        }


        textTitle = (TextView) view.findViewById(R.id.textName);
        albumImage = (ImageView) view.findViewById(R.id.albumImage);
        equalizerGif = (pl.droidsonroids.gif.GifImageView) view.findViewById(R.id.equalizerGif);

        textTitle.setTextColor(this.getResources().getColor(R.color.playingColor));

        if (MusicService.mediaPlayer.isPlaying()) {
            albumImage.setVisibility(View.GONE);
            equalizerGif.setVisibility(View.VISIBLE);
        }
    }

    private void setPause(Boolean pause) {
        view = lv.getChildAt(MusicService.actualPlayingPosition - lv.getFirstVisiblePosition());

        if (view == null)
            return;

        albumImage = (ImageView) view.findViewById(R.id.albumImage);
        equalizerGif = (pl.droidsonroids.gif.GifImageView) view.findViewById(R.id.equalizerGif);

        if (pause) {
            equalizerGif.setVisibility(View.VISIBLE);
            albumImage.setVisibility(View.VISIBLE);
        }
        else {
            albumImage.setVisibility(View.GONE);
            equalizerGif.setVisibility(View.VISIBLE);
        }
    }

}
