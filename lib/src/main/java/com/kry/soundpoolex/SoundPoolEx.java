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

    private SoundPoolEx(SoundPool soundPool) {
        mDelegate = soundPool;
        mMediaPlayer = new MediaPlayer();
        mSoundIds = new HashMap<>();
        mStreamIds = new HashMap<>();
    }

    /**
     * Load the sound from the specified path.
     *
     * @param path
     *         the path to the audio file
     * @param priority
     *         the priority of the sound. Currently has no effect. Use a value of 1 for future
     *         compatibility.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
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

    /**
     * Load the sound from the specified APK resource.
     * <p/>
     * Note that the extension is dropped. For example, if you want to load a sound from the raw
     * resource file "explosion.mp3", you would specify "R.raw.explosion" as the resource ID. Note
     * that this means you cannot have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     * directory.
     *
     * @param context
     *         the application context
     * @param resId
     *         the resource ID
     * @param priority
     *         the priority of the sound
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    public int load(Context context, int resId, int priority) {
        int duration = calcDuration(context, resId);
        int soundID = mDelegate.load(context, resId, priority);
        mSoundIds.put(soundID, duration);
        return soundID;
    }

    /**
     * Load the sound from an asset file descriptor.
     *
     * @param afd
     *         an asset file descriptor
     * @param priority
     *         the priority of the sound. Currently has no effect. Use a value of 1 for future
     *         compatibility.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    @TargetApi (Build.VERSION_CODES.CUPCAKE)
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

    /**
     * Load the sound from a FileDescriptor.
     * <p/>
     * This version is useful if you store multiple sounds in a single binary. The offset specifies
     * the offset from the start of the file and the length specifies the length of the sound within
     * the file.
     *
     * @param fd
     *         a FileDescriptor object
     * @param offset
     *         offset to the start of the sound
     * @param length
     *         length of the sound
     * @param priority
     *         the priority of the sound. Currently has no effect. Use a value of 1 for future
     *         compatibility.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    @TargetApi (Build.VERSION_CODES.CUPCAKE)
    public int load(FileDescriptor fd, long offset, long length, int priority) {
        int duration = calcDuration(fd, offset, length);
        int soundID = mDelegate.load(fd, offset, length, priority);
        mSoundIds.put(soundID, duration);
        return soundID;
    }

    /**
     * Unload a sound from a sound ID.
     * <p/>
     * Unloads the sound specified by the soundID. This is the value returned by the load()
     * function. Returns true if the sound is successfully unloaded, false if the sound was already
     * unloaded.
     *
     * @param soundID
     *         a soundID returned by the load() function
     * @return true if just unloaded, false if previously unloaded
     */
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

    /**
     * Play a sound from a sound ID.
     * <p/>
     * Play the sound specified by the soundID. This is the value returned by the load() function.
     * Returns a non-zero streamID if successful, zero if it fails. The streamID can be used to
     * further control playback. Note that calling play() may cause another sound to stop playing if
     * the maximum number of active streams is exceeded. A loop value of -1 means loop forever, a
     * value of 0 means don't loop, other values indicate the number of repeats, e.g. a value of 1
     * plays the audio twice. The playback rate allows the application to vary the playback rate
     * (pitch) of the sound. A value of 1.0 means play back at the original frequency. A value of
     * 2.0 means play back twice as fast, and a value of 0.5 means playback at half speed.
     *
     * @param soundID
     *         a soundID returned by the load() function
     * @param leftVolume
     *         left volume value (range = 0.0 to 1.0)
     * @param rightVolume
     *         right volume value (range = 0.0 to 1.0)
     * @param priority
     *         stream priority (0 = lowest priority)
     * @param loop
     *         loop mode (0 = no loop, -1 = loop forever)
     * @param rate
     *         playback rate (1.0 = normal playback, range 0.5 to 2.0)
     * @return non-zero streamID if successful, zero if failed
     */
    public int play(int soundID, float leftVolume, float rightVolume, int priority, int loop,
            float rate) {
        int streamID = mDelegate.play(soundID, leftVolume, rightVolume, priority, loop, rate);

        if (streamID > 0) {
            SoundBundle bundle = new SoundBundle(streamID, soundID, mSoundIds.get(soundID));
            bundle.play(loop, rate);
        }
        return streamID;
    }

    /**
     * Pause a playback stream.
     * <p/>
     * Pause the stream specified by the streamID. This is the value returned by the play()
     * function. If the stream is playing, it will be paused. If the stream is not playing (e.g. is
     * stopped or was previously paused), calling this function will have no effect.
     *
     * @param streamID
     *         a streamID returned by the play() function
     */
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

    /**
     * Resume a playback stream.
     * <p/>
     * Resume the stream specified by the streamID. This is the value returned by the play()
     * function. If the stream is paused, this will resume playback. If the stream was not
     * previously paused, calling this function will have no effect.
     *
     * @param streamID
     *         a streamID returned by the play() function
     */
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

    /**
     * Pause all active streams.
     * <p/>
     * Pause all streams that are currently playing. This function iterates through all the active
     * streams and pauses any that are playing. It also sets a flag so that any streams that are
     * playing can be resumed by calling autoResume().
     */
    @TargetApi (Build.VERSION_CODES.FROYO)
    public void autoPause() {
        mDelegate.autoPause();

        if (!mStreamIds.isEmpty()) {
            for (SoundBundle bundle : mStreamIds.values()) {
                bundle.pause();
            }
        }
    }

    /**
     * Resume all previously active streams.
     * <p/>
     * Automatically resumes all streams that were paused in previous calls to autoPause().
     */
    @TargetApi (Build.VERSION_CODES.FROYO)
    public void autoResume() {
        mDelegate.autoResume();

        if (!mStreamIds.isEmpty()) {
            for (SoundBundle bundle : mStreamIds.values()) {
                bundle.resume();
            }
        }
    }

    /**
     * Stop a playback stream.
     * <p/>
     * Stop the stream specified by the streamID. This is the value returned by the play() function.
     * If the stream is playing, it will be stopped. It also releases any native resources
     * associated with this stream. If the stream is not playing, it will have no effect.
     *
     * @param streamID
     *         a streamID returned by the play() function
     */
    public void stop(int streamID) {
        mDelegate.stop(streamID);

        if (streamID > 0) {
            SoundBundle bundle = mStreamIds.get(streamID);
            if (bundle != null) {
                bundle.stop();
            } else {
                Log.d(TAG, "SoundBundle for stream ID:" + String.valueOf(streamID) + " don't " +
                        "exists");
            }
        }
    }

    /**
     * Set stream volume.
     * <p/>
     * Sets the volume on the stream specified by the streamID. This is the value returned by the
     * play() function. The value must be in the range of 0.0 to 1.0. If the stream does not exist,
     * it will have no effect.
     *
     * @param streamID
     *         a streamID returned by the play() function
     * @param leftVolume
     *         left volume value (range = 0.0 to 1.0)
     * @param rightVolume
     *         right volume value (range = 0.0 to 1.0)
     */
    public void setVolume(int streamID, float leftVolume, float rightVolume) {
        mDelegate.setVolume(streamID, leftVolume, rightVolume);
    }

    /**
     * Set volume of all channels to same value.
     */
    public void setVolume(int streamID, float volume) {
        setVolume(streamID, volume, volume);
    }

    /**
     * Change stream priority.
     * <p/>
     * Change the priority of the stream specified by the streamID. This is the value returned by
     * the play() function. Affects the order in which streams are re-used to play new sounds. If
     * the stream does not exist, it will have no effect.
     *
     * @param streamID
     *         a streamID returned by the play() function
     */
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

    /**
     * Sets the callback hook for the OnLoadCompleteListener.
     */
    @TargetApi (Build.VERSION_CODES.FROYO)
    public void setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener listener) {
        mDelegate.setOnLoadCompleteListener(listener);
    }

    /**
     * Release the SoundPoolEx resources.
     * <p/>
     * Release all memory and native resources used by the SoundPoolEx object. The SoundPoolEx can
     * no longer be used and the reference should be set to null.
     */
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
        if (streamID <= 0 || !mSoundIds.containsKey(streamID)) return false;
        return mStreamIds.get(streamID).isPlaying();
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
