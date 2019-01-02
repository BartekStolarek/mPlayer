package stolarek.bartosz.mplayer2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import static android.media.MediaPlayer.create;

public class MusicService extends Service {
    public static Runnable runnable = null;
    public static MediaPlayer mediaPlayer;
    Handler updateHandler;

    String received, notificationMessage;
    Integer seekTo, position;
    public static Integer actualPlayingPosition;
    NotificationCompat.Builder notification;
    NotificationManager notificationManager;
    PendingIntent pendingIntent;
    Intent playerActivity;
    Boolean retrieveSong, checkNextSong, checkPreviousSong;
    public static Boolean playing;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        updateHandler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                sendMessage(mediaPlayer.getCurrentPosition(), "currentSpeed");
                updateHandler.postDelayed(this, 1000);
            }
        };

        playerActivity = new Intent(this, PlayerActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, playerActivity, 0);
        playing = false;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        notificationManager.cancelAll();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        received = intent.getStringExtra("MusicService.data");
        seekTo = intent.getIntExtra("seekTo", 0);
        position = intent.getIntExtra("SongPosition", -1);
        retrieveSong = intent.getBooleanExtra("retrieveSong", false);
        checkNextSong = intent.getBooleanExtra("checkNextSong", false);
        checkPreviousSong = intent.getBooleanExtra("checkPreviousSong", false);

        if (checkNextSong) {
            checkNextSong();
        }

        if (checkPreviousSong) {
            checkPreviousSong();
        }

        if (retrieveSong) {
            sendMessage(actualPlayingPosition, "retrieveSong");

            if (mediaPlayer != null)
                sendMessage(mediaPlayer.getDuration(), "maxDuration");
        }

        if (position >= 0) {
            if (mediaPlayer != null)
                mediaPlayer.release();

            mediaPlayer = create(this, Uri.parse(MainActivity.songsList.get(position).getUrl()));
            prepareSong();

            sendNotification(position);
            actualPlayingPosition = position;
        }

        if (seekTo > 1) {
            mediaPlayer.seekTo(seekTo);
        }

        if (received instanceof String) {
            if (received.equals("start")) {
                mediaPlayer.start();
                updateHandler.postDelayed(runnable,1000);
                sendMessage(mediaPlayer.getDuration(), "maxDuration");
                sendMessage(1, "started");
                playing = true;
            }
            else if (received.equals("pause")) {
                mediaPlayer.pause();
                sendMessage(1, "paused");
                sendMessage(mediaPlayer.getCurrentPosition(), "currentSpeed");
                playing = false;
            }
        }

        return START_NOT_STICKY;
    }

    public void prepareSong() {
        try {
            mediaPlayer.prepare();
        } catch (Exception e) {}
        mediaPlayer.start();
        updateHandler.postDelayed(runnable,1000);
        sendMessage(mediaPlayer.getDuration(), "maxDuration");
        setPlayerListener();
    }

    public void setPlayerListener() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                checkNextSong();
            }
        });
    }

    public void checkPreviousSong() {
        if (actualPlayingPosition - 1 >= 0 && MainActivity.songsList.get(actualPlayingPosition - 1) != null) {
            actualPlayingPosition--;
        }
        else {
            actualPlayingPosition = MainActivity.songsList.size() - 1;
        }
        if (mediaPlayer != null)
            mediaPlayer.release();

        mediaPlayer = create(this, Uri.parse(MainActivity.songsList.get(actualPlayingPosition).getUrl()));
        prepareSong();
        sendNotification(actualPlayingPosition);
        sendMessage(actualPlayingPosition, "retrieveSong");
    }

    public void checkNextSong() {
        if (MainActivity.songsList.size() > actualPlayingPosition + 1 && MainActivity.songsList.get(actualPlayingPosition + 1) != null) {
            actualPlayingPosition++;
        }
        else {
            actualPlayingPosition = 0;
        }
        if (mediaPlayer != null)
            mediaPlayer.release();

        mediaPlayer = create(this, Uri.parse(MainActivity.songsList.get(actualPlayingPosition).getUrl()));
        prepareSong();
        sendNotification(actualPlayingPosition);
        sendMessage(actualPlayingPosition, "retrieveSong");
    }

    private void sendMessage(Integer msg, String propertyName) {
        Intent intent = new Intent(propertyName);
        sendLocationBroadcast(intent, msg, propertyName);
    }

    private void sendLocationBroadcast(Intent intent, Integer msg, String propertyName){
        intent.putExtra(propertyName, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendNotification(int position) {
        if (!MainActivity.songsList.get(position).getArtist().equals("Unknown Artist"))
            notificationMessage = MainActivity.songsList.get(position).getArtist() + " - " + MainActivity.songsList.get(position).getName();
        else
            notificationMessage = MainActivity.songsList.get(position).getName();

        notification =
                new NotificationCompat.Builder(this, "channel")
                        .setSmallIcon(R.drawable.album)
                        .setContentTitle("Playing now:")
                        .setContentText(notificationMessage)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001, notification.build());
    }
}