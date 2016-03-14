package com.kry.soundpoolex;

import android.annotation.TargetApi;
import android.media.SoundPool;
import android.os.Build;

public class SoundPoolAdapter extends SoundPool implements ISoundPool {
    /**
     * Constructor. Constructs a SoundPool object with the following characteristics:
     *
     * @param maxStreams
     *         the maximum number of simultaneous streams for this SoundPool object
     * @param streamType
     *         the audio stream type as described in AudioManager For example, game applications
     *         will normally use {@link android.media.AudioManager#STREAM_MUSIC}.
     * @param srcQuality
     *         the sample-rate converter quality. Currently has no effect. Use 0 for the default.
     * @deprecated use {@link Builder} instead to create and configure a SoundPool instance
     */
    @Deprecated
    public SoundPoolAdapter(int maxStreams, int streamType, int srcQuality) {
        super(maxStreams, streamType, srcQuality);
    }

    @Override
    public void setVolume(int streamID, float volume) {
        super.setVolume(streamID, volume, volume);
    }

    @TargetApi (Build.VERSION_CODES.LOLLIPOP)
    public static class Builder extends SoundPool.Builder {

        @Override
        public SoundPoolAdapter build() {
            return (SoundPoolAdapter) super.build();
        }
    }

}
