package com.kry.soundpoolex;

/**
 * Bundle class for saving playing status of a SoundPoolEx sounds
 */
public class SoundBundle {
    private static final float MIN_RATE = 0.5f;
    private static final float MAX_RATE = 2.0f;

    private final int streamID;
    private final int soundID;
    private final int duration;
    private boolean playing;
    private long startPlayingTime;
    private volatile long endPlayingTime;
    private long onPauseTime;
    private int loop;
    private float rate;
    private volatile int nonPlayedTime;

    /**
     * Creates SoundBundle from soundID and stores duration into.
     *
     * @param streamID
     *         a streamID returned by the SoundPoolEx.play() function
     * @param soundID
     *         a soundID returned by the SoundPoolEx.load() function
     * @param duration
     *         a duration in milliseconds
     */
    SoundBundle(int streamID, int soundID, int duration) {
        this.streamID = streamID;
        this.soundID = soundID;
        this.duration = duration;

        //default rate
        this.rate = 1f;
    }

    /**
     * Stores the playing state in the SoundBundle
     *
     * @param loop
     *         loop mode (0 = no loop, -1 = loop forever)
     * @param rate
     *         playback rate
     */
    protected void play(int loop, float rate) {
        if (isPlaying()) stop();
        this.loop = loop;
        this.rate = rate;
        nonPlayedTime = 0;
        startPlayingTime = System.currentTimeMillis();
        if (loop >= 0) {
            endPlayingTime = startPlayingTime + getDuration();
        }
        playing = true;
    }

    /**
     * Returns the playing state stored in the SoundBundle
     *
     * @return true if currently playing, false otherwise
     */
    protected boolean isPlaying() {
        if (!playing) return false;
        if (loop == -1) return true;
        //check if playing time is over
        if (System.currentTimeMillis() >= endPlayingTime) {
            stop();
            return false;
        }
        return true;
    }

    /**
     * Stores the stopped state in the SoundBundle
     */
    protected void stop() {
        playing = false;
        rate = 1f;
        loop = 0;
        startPlayingTime = 0;
        endPlayingTime = 0;
        onPauseTime = 0;
    }

    /**
     * Gets the duration stored in the SoundBundle.
     *
     * @return the duration or 0 if playback mode is infinity loop
     */
    protected int getDuration() {
        //return 0 if loop == -1 (infinity)
        return (int) (duration / rate * (loop + 1));
    }

    /**
     * Stores the paused state in the SoundBundle
     */
    protected void pause() {
        if (isPlaying()) {
            playing = false;
            onPauseTime = System.currentTimeMillis();
        }
    }

    /**
     * Stores the resumed (playing) state in the SoundBundle
     */
    protected void resume() {
        if (onPauseTime > 0 && endPlayingTime > 0 && !isPlaying()) {
            final long now = System.currentTimeMillis();
            nonPlayedTime += (now - onPauseTime);
            if (loop >= 0) {
                endPlayingTime = now + (endPlayingTime - onPauseTime);
            }
            playing = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoundBundle that = (SoundBundle) o;

        if (streamID != that.streamID) return false;
        if (soundID != that.soundID) return false;
        if (duration != that.duration) return false;
        if (playing != that.playing) return false;
        if (startPlayingTime != that.startPlayingTime) return false;
        return endPlayingTime == that.endPlayingTime;
    }

    @Override
    public int hashCode() {
        return streamID;
    }

    /**
     * Returns the streamID
     *
     * @return the streamID
     */
    protected int getStreamID() {
        return streamID;
    }

    /**
     * Returns the soundID
     *
     * @return the soundID
     */
    protected int getSoundID() {
        return soundID;
    }

    /**
     * Change playback rate and recalculate new end playing time.
     *
     * @param rate
     *         playback rate (1.0 = normal playback, range 0.5 to 2.0)
     */
    public void setRate(float rate) {
        float newRate;
        if (rate < MIN_RATE) {
            newRate = MIN_RATE;
        } else if (rate > MAX_RATE) {
            newRate = MAX_RATE;
        } else {
            newRate = rate;
        }

        endPlayingTime = recalcEndPlayingTime(loop, newRate);
        this.rate = newRate;
    }

    private long recalcEndPlayingTime(int newLoop, float newRate) {
        if (BuildConfig.DEBUG && !(newLoop == loop || newRate == rate)) throw new AssertionError();

        if (newLoop == loop && newRate == rate) return endPlayingTime;
        if (newLoop < 0) return 0;
        if (startPlayingTime == 0) return 0;
        if (getDuration() == 0) return 0;

        int singleRunDuration = getSingleRunDuration();

        //duration of the sound when the new rate
        int newSingleRunDuration = (int) (duration / newRate);

        long now = System.currentTimeMillis();

        int playedTime = getPlayedTime(now);

        //integer number of the played loops
        int loopsPlayed = playedTime / singleRunDuration;
        //the elapsed playing time of the current run
        int currentRunPlayedTime = playedTime - (singleRunDuration * loopsPlayed);

        if (newLoop != loop) {
            int newLoopsCount = newLoop + 1;
            int currentRunElapsedTime = singleRunDuration - currentRunPlayedTime;

            if (loopsPlayed >= newLoopsCount) {
                return now + currentRunElapsedTime;
            } else {
                return now + (newLoopsCount - loopsPlayed) * singleRunDuration +
                        currentRunElapsedTime;
            }
        }

        if (newRate != rate) {
            int loopCount = loop + 1;
            //the part of elapsed playing time in total time of the current run
            float currentRunPlayedPart = currentRunPlayedTime / singleRunDuration;

            //the time of the unplayed loops when the new rate
            int remainingLoopsPlayingTime = (loopCount - loopsPlayed) * newSingleRunDuration;
            //the elapsed playing time of the current run when the new rate
            int newCurrentRunPlayedTime = (int) (currentRunPlayedPart * newSingleRunDuration);

            return now + (remainingLoopsPlayingTime - newCurrentRunPlayedTime);
        }

        return 0;
    }

    /**
     * Returns duration of a single loop
     *
     * @return duration of a single loop
     */
    private int getSingleRunDuration() {
        return (int) (duration / rate);
    }

    /**
     * Returns the duration of sound playback till the timestamp
     *
     * @param timestamp
     *         the timestamp of which the duration is determined relative to
     * @return the duration of sound playback till the timestamp
     */
    private int getPlayedTime(long timestamp) {
        return (int) ((timestamp - startPlayingTime) - nonPlayedTime);
    }

    public void setLoop(int loop) {
        int newLoop;
        if (loop < -1) {
            newLoop = -1;
        } else {
            newLoop = loop;
        }

        endPlayingTime = recalcEndPlayingTime(newLoop, rate);
        this.loop = loop;
    }
}
