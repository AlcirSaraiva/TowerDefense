package com.awesome.towerdefense;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public class Audio {
    private static SoundPool soundPool;
    private Context context;
    private long lastTime, soundGap = 200;
    private MediaPlayer soundtrack;

    public void initAudio(Context c) {
        context = c;
        AudioAttributes audioAttributes = new AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool
                .Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();
        lastTime = System.currentTimeMillis();
        soundtrack = MediaPlayer.create(context, R.raw.audio_track0);
        soundtrack.setLooping(true);
        try {
            soundtrack.setDataSource(context.getAssets().openFd("raw/audio_track0.mp3").getFileDescriptor());
            soundtrack.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int load(int fileId) {
        int result = 0;
        if (context != null) result = soundPool.load(context, fileId, 1);
        return result;
    }

    public void playTrack() {
        try {
            soundtrack.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void pauseTrack() {
        try {
            soundtrack.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void play(int soundId, boolean priority, long now) {
        if (priority) {
            lastTime = now;
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
        } else if (now - lastTime > soundGap) {
            lastTime = now;
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void stop() {
        soundtrack.stop();
        try {
            soundtrack.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unload() {
        soundPool.release();
        soundPool = null;
        soundtrack.release();
        soundtrack = null;
    }
}
