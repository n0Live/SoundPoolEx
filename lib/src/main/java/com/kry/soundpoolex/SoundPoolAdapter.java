package com.kry.soundpoolex;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

import java.io.FileDescriptor;

public class SoundPoolAdapter implements ISoundPool {

    private final SoundPool mSoundPoolImpl;

    /**
     * Constructor. Constructs a SoundPoolAdapter object with the following characteristics:
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
        mSoundPoolImpl = new SoundPool(maxStreams, streamType, srcQuality);
    }

    /**
     * Constructor. Constructs a SoundPoolAdapter object from an existing SoundPool object.
     */
    private SoundPoolAdapter(SoundPool soundPoolImpl) {
        mSoundPoolImpl = soundPoolImpl;
    }

    @Override
    public int load(String path, int priority) {
        return mSoundPoolImpl.load(path, priority);
    }

    @Override
    public int load(Context context, int resId, int priority) {
        return mSoundPoolImpl.load(context, resId, priority);
    }

    @TargetApi (Build.VERSION_CODES.CUPCAKE)
    @Override
    public int load(AssetFileDescriptor afd, int priority) {
        return mSoundPoolImpl.load(afd, priority);
    }

    @TargetApi (Build.VERSION_CODES.CUPCAKE)
    @Override
    public int load(FileDescriptor fd, long offset, long length, int priority) {
        return mSoundPoolImpl.load(fd, offset, length, priority);
    }

    @Override
    public boolean unload(int soundID) {
        return mSoundPoolImpl.unload(soundID);
    }

    @Override
    public int play(int soundID, float leftVolume, float rightVolume, int priority, int loop,
            float rate) {
        return mSoundPoolImpl.play(soundID, leftVolume, rightVolume, priority, loop, rate);
    }

    @Override
    public void pause(int streamID) {
        mSoundPoolImpl.pause(streamID);
    }

    @Override
    public void resume(int streamID) {
        mSoundPoolImpl.resume(streamID);
    }

    @TargetApi (Build.VERSION_CODES.FROYO)
    @Override
    public void autoPause() {
        mSoundPoolImpl.autoPause();
    }

    @TargetApi (Build.VERSION_CODES.FROYO)
    @Override
    public void autoResume() {
        mSoundPoolImpl.autoResume();
    }

    @Override
    public void stop(int streamID) {
        mSoundPoolImpl.stop(streamID);
    }

    @Override
    public void setVolume(int streamID, float leftVolume, float rightVolume) {
        mSoundPoolImpl.setVolume(streamID, leftVolume, rightVolume);
    }

    @Override
    public void setVolume(int streamID, float volume) {
        mSoundPoolImpl.setVolume(streamID, volume, volume);
    }

    @Override
    public void setPriority(int streamID, int priority) {
        mSoundPoolImpl.setPriority(streamID, priority);
    }

    @Override
    public void setLoop(int streamID, int loop) {
        mSoundPoolImpl.setLoop(streamID, loop);
    }

    @Override
    public void setRate(int streamID, float rate) {
        mSoundPoolImpl.setRate(streamID, rate);
    }

    @TargetApi (Build.VERSION_CODES.FROYO)
    @Override
    public void setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener listener) {
        mSoundPoolImpl.setOnLoadCompleteListener(listener);
    }

    @Override
    public void release() {
        mSoundPoolImpl.release();
    }

    /**
     * Builder class for {@link SoundPoolAdapter} objects.
     */
    @TargetApi (Build.VERSION_CODES.LOLLIPOP)
    public static class Builder {
        private final SoundPool.Builder mBuilderImpl;

        /**
         * Constructs a new Builder with the defaults format values. If not provided, the maximum
         * number of streams is 1 (see {@link #setMaxStreams(int)} to change it), and the audio
         * attributes have a usage value of {@link AudioAttributes#USAGE_MEDIA} (see {@link
         * #setAudioAttributes(AudioAttributes)} to change them).
         */
        public Builder() {
            mBuilderImpl = new SoundPool.Builder();
        }

        /**
         * Sets the maximum of number of simultaneous streams that can be played simultaneously.
         *
         * @param maxStreams
         *         a value equal to 1 or greater.
         * @return the same Builder instance
         * @throws IllegalArgumentException
         */
        public Builder setMaxStreams(int maxStreams) throws IllegalArgumentException {
            mBuilderImpl.setMaxStreams(maxStreams);
            return this;
        }

        /**
         * Sets the {@link AudioAttributes}. For examples, game applications will use attributes
         * built with usage information set to {@link AudioAttributes#USAGE_GAME}.
         *
         * @param attributes
         *         a non-null
         * @return the same Builder instance
         */
        @TargetApi (Build.VERSION_CODES.LOLLIPOP)
        public Builder setAudioAttributes(AudioAttributes attributes) throws
                IllegalArgumentException {
            mBuilderImpl.setAudioAttributes(attributes);
            return this;
        }

        public SoundPoolAdapter build() {
            SoundPool soundPool = mBuilderImpl.build();
            return new SoundPoolAdapter(soundPool);
        }
    }

}
