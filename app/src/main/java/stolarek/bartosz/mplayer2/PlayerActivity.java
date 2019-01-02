package stolarek.bartosz.mplayer2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity {
    ImageView previousButton, nextButton, startButton;
    TextView timeElapsed, timeAll, textTitle, textArtist;
    Boolean playing;
    SeekBar seekBar;
    Integer position;
    Intent musicService, myIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Initialize UI handlers
        previousButton = (ImageView) findViewById(R.id.previousTrack);
        startButton = (ImageView) findViewById(R.id.startMusic);
        nextButton = (ImageView) findViewById(R.id.nextTrack);
        timeElapsed = (TextView) findViewById(R.id.timeElapsed);
        timeAll = (TextView) findViewById(R.id.timeAll);
        textTitle = (TextView) findViewById(R.id.textTitle);
        textArtist = (TextView) findViewById(R.id.textArtist);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        playing = true;

        musicService = new Intent(this, MusicService.class);

        //Get Extra Values received from MainActivity, which is position. MainActivity is sending position of song (which user has clicked) to let PlayerActivity know
        //which song initialize. If song position is sent, means that user has clicked on song in MainActivity, if song position is not sent, means that PlayerActivity
        //was initialized by notification and is actually playing a song- so "retrieveSong" parameter is sent to MusicService, and service is answering with name and acutal
        //position of actual playing song (to display them in PlayerActivity).
        myIntent = getIntent();
        position = myIntent.getIntExtra("SongPosition", -1);

        if (position >= 0) {
            musicService.putExtra("SongPosition", position);
            musicService.putExtra("MusicService.data", "start");
            startService(musicService);

            musicService.removeExtra("MusicService.data");
            musicService.removeExtra("SongPosition");

            textTitle.setText(MainActivity.songsList.get(position).getName());
            textArtist.setText(MainActivity.songsList.get(position).getArtist());
        }
        else {
            musicService.putExtra("retrieveSong", true);
            startService(musicService);
            musicService.removeExtra("retrieveSong");

            if (!MusicService.mediaPlayer.isPlaying()) {
                startButton.setImageResource(R.drawable.play);
                playing = false;
            }
        }

        //Registering receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("currentSpeed"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("maxDuration"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("retrieveSong"));

        //Methods connected with seekbar- to rewind etc.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    musicService.putExtra("seekTo", i);
                    startService(musicService);
                    musicService.removeExtra("seekTo");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    //BroadcastReceiver is listening for messages- if a message with an Extra is sent, it can do some things
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Integer currentPosition = intent.getIntExtra("currentSpeed", 20);
            Integer maxDuration = intent.getIntExtra("maxDuration", 20);
            Integer retrieveSong = intent.getIntExtra("retrieveSong", -1);

            if (retrieveSong >= 0) {
                textTitle.setText(MainActivity.songsList.get(retrieveSong).getName());
                textArtist.setText(MainActivity.songsList.get(retrieveSong).getArtist());
            }
            if (maxDuration != 20) {
                seekBar.setMax(maxDuration);
                timeAll.setText(formatTime(maxDuration));
            }
            if (currentPosition != 20) {
                seekBar.setProgress(currentPosition);
                timeElapsed.setText(formatTime(currentPosition));
            }
        }
    };

    //formatTime function is just formatting miliseconds into minutes:seconds
    public String formatTime(int miliseconds) {
        long milis = miliseconds;
        long seconds = (milis / 1000) % 60;
        long minutes = (milis / (1000 * 60)) % 60;
        String tmpMinutes = Long.toString(minutes);
        String tmpSeconds = Long.toString(seconds);

        if (tmpMinutes.length() < 2)
            tmpMinutes = "0" + tmpMinutes;
        if (tmpSeconds.length() < 2)
            tmpSeconds = "0" + tmpSeconds;

        return tmpMinutes + ":" + tmpSeconds;
    }

    //playMusic function is initialized when user clicks on "Play" or "Pause" button- first function is checking if music is playing or not, and then
    //it's sending info to MusicService- to play music or not. And finally function is changing image and boolean value "playing".
    public void playMusic(View v) {
        if (playing) {
            musicService.putExtra("MusicService.data", "pause");
            startService(musicService);
            startButton.setImageResource(R.drawable.play);
            playing = false;
        }
        else {
            musicService.putExtra("MusicService.data", "start");
            startService(musicService);
            startButton.setImageResource(R.drawable.pause);
            playing = true;
        }
    }

    public void playNextSong(View v) {
        checkNextSong();
    }

    public void playPreviousSong(View v) {
        checkPreviousSong();
    }

    //checkNextSong and checkPreviousSong functions are initializing MusicService functions- MusicService is checking if
    //next/previous song can be played- if yes, play it- if no, play song[0] or song[last]
    public void checkNextSong() {
        musicService.putExtra("checkNextSong", true);
        startService(musicService);
        musicService.removeExtra("checkNextSong");
    }

    public void checkPreviousSong() {
        musicService.putExtra("checkPreviousSong", true);
        startService(musicService);
        musicService.removeExtra("checkPreviousSong");
    }
}