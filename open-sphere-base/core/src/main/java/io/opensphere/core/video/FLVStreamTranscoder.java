package io.opensphere.core.video;

import io.opensphere.core.util.io.CancellableInputStream;

/** Interface for FLV video stream transcoders. */
public interface FLVStreamTranscoder
{
    /**
     * Sets the object to notify of any unrecoverable errors.
     *
     * @param handler The object to notify of errors.
     */
    void setErrorHandler(VideoErrorHandler handler);

    /**
     * Transcode the video in the provided stream into FLV. If the given stream
     * already provided FLV video, the returned stream will provided only the
     * data in the original stream (no transcoding).
     *
     * @param videoIn The video stream to transcode.
     * @return A stream which provides FLV video.
     */
    CancellableInputStream transcodeToFLV(CancellableInputStream videoIn);
}
