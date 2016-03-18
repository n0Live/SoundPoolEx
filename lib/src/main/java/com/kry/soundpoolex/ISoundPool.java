package com.kry.soundpoolex;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.SoundPool;

import java.io.FileDescriptor;

/**
 * Interface for SoundPool implementations.
 */
public interface ISoundPool {
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
    int load(String path, int priority);

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
     *         the priority of the sound. Currently has no effect. Use a value of 1 for future
     *         compatibility.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    int load(Context context, int resId, int priority);

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
    int load(AssetFileDescriptor afd, int priority);

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
    int load(FileDescriptor fd, long offset, long length, int priority);

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
    boolean unload(int soundID);

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
    int play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate);

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
    void pause(int streamID);

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
    void resume(int streamID);

    /**
     * Pause all active streams.
     * <p/>
     * Pause all streams that are currently playing. This function iterates through all the active
     * streams and pauses any that are playing. It also sets a flag so that any streams that are
     * playing can be resumed by calling autoResume().
     */
    void autoPause();

    /**
     * Resume all previously active streams.
     * <p/>
     * Automatically resumes all streams that were paused in previous calls to autoPause().
     */
    void autoResume();

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
    void stop(int streamID);

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
    void setVolume(int streamID, float leftVolume, float rightVolume);

    /**
     * Similar {@link #setVolume(int, float, float)}, except set volume of all channels to same
     * value.
     */
    void setVolume(int streamID, float volume);

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
    void setPriority(int streamID, int priority);

    /**
     * Set loop mode.
     * <p/>
     * Change the loop mode. A loop value of -1 means loop forever, a value of 0 means don't loop,
     * other values indicate the number of repeats, e.g. a value of 1 plays the audio twice. If the
     * stream does not exist, it will have no effect.
     *
     * @param streamID
     *         a streamID returned by the play() function
     * @param loop
     *         loop mode (0 = no loop, -1 = loop forever)
     */
    void setLoop(int streamID, int loop);

    /**
     * Change playback rate.
     * <p/>
     * The playback rate allows the application to vary the playback rate (pitch) of the sound. A
     * value of 1.0 means playback at the original frequency. A value of 2.0 means playback twice as
     * fast, and a value of 0.5 means playback at half speed. If the stream does not exist, it will
     * have no effect.
     *
     * @param streamID
     *         a streamID returned by the play() function
     * @param rate
     *         playback rate (1.0 = normal playback, range 0.5 to 2.0)
     */
    void setRate(int streamID, float rate);

    /**
     * Sets the callback hook for the OnLoadCompleteListener.
     */
    void setOnLoadCompleteListener(SoundPool.OnLoadCompleteListener listener);

    /**
     * Release the SoundPool resources.
     * <p/>
     * Release all memory and native resources used by the SoundPool object. The SoundPool can no
     * longer be used and the reference should be set to null.
     */
    void release();
}
