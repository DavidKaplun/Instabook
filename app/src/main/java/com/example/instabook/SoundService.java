package com.example.instabook;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

public class SoundService extends Service {
    // declaring object of MediaPlayer
    private MediaPlayer player;


    // execution of service will start
    // on calling this method
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // creating a media player which
        // will play the audio of Default
        // ringtone in android device
        player = MediaPlayer.create( this, R.raw.sound2);
        player.start();// starting the process

        // returns the status
        // of the program
        return START_STICKY;
    }



    // execution of the service will
    // stop on calling this method
    @Override
    public void onDestroy() {
        super.onDestroy();

        // stopping the process
        player.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
