package com.kry.soundpoolex;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.SoundPool;

import java.io.FileDescriptor;

/**
 * Created by noLive on 14.05.2015.
 */
public interface ISoundPool {
    int load(String path, int priority);

    int load(Context context, int resId, int priority);

    int load(AssetFileDescriptor afd, int priority);

    int load(FileDescriptor fd, long offset, long length, int priority);

    boolean unload(int soundID);

    int play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate);

    void pause(int streamID);

    void resume(int streamID);

    void autoPause();

    void autoResume();

    void stop(int streamID);

    void setVolume(int streamID, float leftVolume, float rightVolume);

    void setVolume(int streamID, float volume);

    void setPriority(int streamID, int priority);

    void setLoop(int streamID, int loop);

    void setRate(int streamID, float rate);

    void setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener listener);

    void release();
}
