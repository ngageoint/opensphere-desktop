package io.opensphere.osh.results.video;

import java.io.IOException;
import java.io.InputStream;

/** Reads a single field from a video stream. */
public interface VideoFieldHandler
{
    /**
     * Reads a single field from a video stream.
     *
     * @param stream the input stream
     * @param videoData the video data to update
     * @return whether EOF was reached
     * @throws IOException if something went wrong reading the stream
     */
    boolean readField(InputStream stream, VideoData videoData) throws IOException;
}
