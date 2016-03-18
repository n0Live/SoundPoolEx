package com.kry.soundpoolex;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The SoundPoolEx extender adds methods {@link #getDuration(int)} and {@link #isPlaying(int)} .
 */
public class SoundPoolEx implements ISoundPool {
    private final static String TAG = "SoundPoolEx";
    private final SoundPool mDelegate;
    private final MediaPlayer mMediaPlayer;
    /**
     * Sound IDs '<'SoundID, Duration'>'
     */
    private final Map<Integer, Integer> mSoundIds;
    /**
     * Stream IDs '<'StreamID, SoundBundle'>'
     */
    private final Map<Integer, SoundBundle> mStreamIds;

    /**
     * Constructor. Constructs a SoundPoolEx object with the following characteristics:
     *
     * @param maxStreams
     *         the maximum number of simultaneous streams for this SoundPoolEx object
     * @param streamType
     *         the audio stream type as described in AudioManager For example, game applications
     *         will normally use {@link AudioManager#STREAM_MUSIC}.
     * @param srcQuality
     *         the sample-rate converter quality. Currently has no effect. Use 0 for the default.
     * @deprecated use {@link Builder} instead to create and configure a SoundPoolEx instance
     */
    @Deprecated
    public SoundPoolEx(int maxStreams, int streamType, int srcQuality) {
        mDelegate = new SoundPool(maxStreams, streamType, srcQuality);
        mMediaPlayer = new MediaPlayer();
        mSoundIds = new HashMap<>();
        mStreamIds = new HashMap<>();
    }

    /**
     * Constructor. Constructs a SoundPoolEx object from an existing SoundPool object.
     */
    private SoundPoolEx(SoundPool soundPool) {
        mDelegate = soundPool;
        mMediaPlayer = new MediaPlayer();
        mSoundIds = new HashMap<>();
        mStreamIds = new HashMap<>();
    }

    @Override
    public int load(String path, int priority) {
        int duration = calcDuration(path);
        int soundID = mDelegate.load(path, priority);
        mSoundIds.put(soundID, duration);
        return soundID;
    }

    /**
     * Calculates the playback duration of the file
     *
     * @param path
     *         the path of the file you want to calc duration
     * @return the duration in milliseconds, if no duration is available, 0 is returned.
     */
    private int calcDuration(String path) {
        int duration = 0;
        try {
            mMediaPlayer.setDataSource(path);
            duration = _calcDuration();
        } catch (IOException e) {
            Log.d(TAG, "MediaPlayer.calcDuration() error:", e);
        } finally {
            mMediaPlayer.reset();
        }
        return duration;
    }

    private int _calcDuration() throws IOException {
        mMediaPlayer.prepare(); //synchronized
        int result = mMediaPlayer.getDuration();
        return (result > 0) ? result : 0;
    }

    @Override
    public int load(Context context, int resId, int priority) {
        int duration = calcDuration(context, resId);
        int soundID = mDelegate.load(context, resId, priority);
        mSoundIds.put(soundID, duration);
        return soundID;
    }

    @TargetApi (Build.VERSION_CODES.CUPCAKE)
    @Override
    public int load(AssetFileDescriptor afd, int priority) {
        int duration = calcDuration(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        final int soundID = mDelegate.load(afd, priority);
        mSoundIds.put(soundID, duration);
        return soundID;
    }

    /**
     * Calculates the playback duration of the file
     *
     * @param fd
     *         the FileDescriptor for the file you want to calc duration
     * @param offset
     *         the offset into the file where the data to be played starts, in bytes
     * @param length
     *         the length in bytes of the data to be played
     * @return the duration in milliseconds, if no duration is available, 0 is returned.
     */
    private int calcDuration(FileDescriptor fd, long offset, long length) {
        int duration = 0;
        try {
            mMediaPlayer.setDataSource(fd, offset, length);
            duration = _calcDuration();
        } catch (IOException e) {
            Log.d(TAG, "MediaPlayer.calcDuration() error:", e);
        } finally {
            mMediaPlayer.reset();
        }
        return duration;
    }

    @TargetApi (Build.VERSION_CODES.CUPCAKE)
    @Override
    public int load(FileDescriptor fd, long offset, long length, int priority) {
        int duration = calcDuration(fd, offset, length);
        int soundID = mDelegate.load(fd, offset, length, priority);
        mSoundIds.put(soundID, duration);
        return soundID;
    }

    @Override
    public boolean unload(int soundID) {
        boolean result = mDelegate.unload(soundID);
        mSoundIds.remove(soundID);

        Iterator<SoundBundle> it = mStreamIds.values().iterator();
        while (it.hasNext()) {
            SoundBundle bundle = it.next();
            if (bundle.getSoundID() == soundID) it.remove();
        }

        return result;
    }

    @Override
    public int play(int soundID, float leftVolume, float rightVolume, int priority, int loop,
            float rate) {
        int streamID = mDelegate.play(soundID, leftVolume, rightVolume, priority, loop, rate);

        if (streamID > 0) {
            SoundBundle bundle = new SoundBundle(streamID, soundID, mSoundIds.get(soundID));
            mStreamIds.put(streamID, bundle);
            bundle.play(loop, rate);
        }
        return streamID;
    }

    @Override
    public void pause(int streamID) {
        mDelegate.pause(streamID);

        if (streamID > 0) {
            SoundBundle bundle = mStreamIds.get(streamID);
            if (bundle != null) {
                bundle.pause();
            } else {
                Log.d(TAG, "SoundBundle for stream ID:" + String.valueOf(streamID) + " don't " +
                        "exists");
            }
        }
    }

    @Override
    public void resume(int streamID) {
        mDelegate.resume(streamID);

        if (streamID > 0) {
            SoundBundle bundle = mStreamIds.get(streamID);
            if (bundle != null) {
                bundle.resume();
            } else {
                Log.d(TAG, "SoundBundle for stream ID:" + String.valueOf(streamID) + " don't " +
                        "exists");
            }
        }
    }

    @TargetApi (Build.VERSION_CODES.FROYO)
    @Override
    public void autoPause() {
        mDelegate.autoPause();

        if (!mStreamIds.isEmpty()) {
            for (SoundBundle bundle : mStreamIds.values()) {
                bundle.pause();
            }
        }
    }

    @TargetApi (Build.VERSION_CODES.FROYO)
    @Override
    public void autoResume() {
        mDelegate.autoResume();

        if (!mStreamIds.isEmpty()) {
            for (SoundBundle bundle : mStreamIds.values()) {
                bundle.resume();
            }
        }
    }

    @Override
    public void stop(int streamID) {
        mDelegate.stop(streamID);

        if (streamID > 0) {
            SoundBundle bundle = mStreamIds.get(streamID);
            if (bundle != null) {
                bundle.stop();
                mStreamIds.remove(streamID);
            } else {
                Log.d(TAG, "SoundBundle for stream ID:" + String.valueOf(streamID) + " don't " +
                        "exists");
            }
        }
    }

    @Override
    public void setVolume(int streamID, float leftVolume, float rightVolume) {
        mDelegate.setVolume(streamID, leftVolume, rightVolume);
    }

    @Override
    public void setVolume(int streamID, float volume) {
        setVolume(streamID, volume, volume);
    }

    @Override
    public final void setPriority(int streamID, int priority) {
        mDelegate.setPriority(streamID, priority);
    }

    @Override
    public void setLoop(int streamID, int loop) {
        mDelegate.setLoop(streamID, loop);

        //setLoop not working on JELLY_BEAN and higher
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && streamID > 0) {
            SoundBundle bundle = mStreamIds.get(streamID);
            if (bundle != null) {
                bundle.setLoop(loop);
            } else {
                Log.d(TAG, "SoundBundle for stream ID:" + String.valueOf(streamID) + " don't " +
                        "exists");
            }
        }
    }

    @Override
    public void setRate(int streamID, float rate) {
        mDelegate.setRate(streamID, rate);

        if (streamID > 0) {
            SoundBundle bundle = mStreamIds.get(streamID);
            if (bundle != null) {
                bundle.setRate(rate);
            } else {
                Log.d(TAG, "SoundBundle for stream ID:" + String.valueOf(streamID) + " don't " +
                        "exists");
            }
        }
    }

    @TargetApi (Build.VERSION_CODES.FROYO)
    @Override
    public void setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener listener) {
        mDelegate.setOnLoadCompleteListener(listener);
    }

    @Override
    public final void release() {
        mDelegate.release();
        mMediaPlayer.release();
        mSoundIds.clear();
        mStreamIds.clear();
    }

    /**
     * Calculates the playback duration of the APK resource
     *
     * @param context
     *         the application context
     * @param resId
     *         the resource ID
     * @return the duration in milliseconds, if no duration is available, 0 is returned.
     */
    private int calcDuration(Context context, int resId) {
        AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
        if (afd == null) return 0;

        int duration = 0;
        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
            duration = _calcDuration();
        } catch (IOException e) {
            Log.d(TAG, "MediaPlayer.calcDuration() error:", e);
        } finally {
            mMediaPlayer.reset();
        }
        return duration;
    }

    /**
     * Gets the duration of the soundID.
     *
     * @return the duration in milliseconds, if no duration is available, 0 is returned.
     */
    public int getDuration(int soundID) {
        if (soundID <= 0 || !mSoundIds.containsKey(soundID)) return 0;
        return mSoundIds.get(soundID);
    }

    /**
     * Gets the duration of the streamID. The duration may vary when playing with different rate and
     * loop mode.
     *
     * @return the duration in milliseconds, if no duration is available, 0 is returned.
     */
    public int getStreamDuration(int streamID) {
        if (streamID <= 0 || !mStreamIds.containsKey(streamID)) return 0;
        return mStreamIds.get(streamID).getDuration();
    }

    /**
     * Checks whether the streamID is playing. May returns false positive result if playing stopped
     * when the maximum number of active streams is exceeded.
     *
     * @return true if currently playing, false otherwise
     */
    public boolean isPlaying(int streamID) {
        if (streamID <= 0 || !mStreamIds.containsKey(streamID)) return false;
        return mStreamIds.get(streamID).isPlaying();
    }

    /**
     * Checks whether any stream of specified soundID is playing. May returns false positive result
     * if playing stopped when the maximum number of active streams is exceeded.
     *
     * @return true if currently playing, false otherwise
     */
    public boolean isSoundPlaying(int soundID) {
        if (soundID <= 0 || !mSoundIds.containsKey(soundID)) return false;

        if (!mStreamIds.isEmpty()) {
            for (SoundBundle bundle : mStreamIds.values()) {
                if (bundle.getSoundID() == soundID) {
                    if (bundle.isPlaying()) return true;
                }
            }
        }

        return false;
    }

    /**
     * Builder class for {@link SoundPoolEx} objects.
     */
    @TargetApi (Build.VERSION_CODES.LOLLIPOP)
    public static class Builder {
        private final SoundPool.Builder builder;

        /**
         * Constructs a new Builder with the defaults format values. If not provided, the maximum
         * number of streams is 1 (see {@link #setMaxStreams(int)} to change it), and the audio
         * attributes have a usage value of {@link AudioAttributes#USAGE_MEDIA} (see {@link
         * #setAudioAttributes(AudioAttributes)} to change them).
         */
        public Builder() {
            builder = new SoundPool.Builder();
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
            builder.setMaxStreams(maxStreams);
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
        public Builder setAudioAttributes(AudioAttributes attributes) throws
                IllegalArgumentException {
            builder.setAudioAttributes(attributes);
            return this;
        }

        public SoundPoolEx build() {
            SoundPool soundPool = builder.build();
            return new SoundPoolEx(soundPool);
        }
    }

}
