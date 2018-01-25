package io.opensphere.core;

/** Manager for handling captured images of the frame buffer. */
public interface FrameBufferCaptureManager
{
    /**
     * Get the capture provider for this manager. This shall not be {@code null}
     * .
     *
     * @return The provider for frame buffer captures.
     */
    FrameBufferCaptureProvider getCaptureProvider();

    /**
     * Set the capture provider for this manager. Only one provider is allowed.
     *
     * @param provider The provider for frame buffer captures.
     */
    void setCaptureProvider(FrameBufferCaptureProvider provider);

    /** Interface for listeners for when a frame buffer capture is produced. */
    @FunctionalInterface
    public interface FrameBufferCaptureListener
    {
        /**
         * Notification to handle when a frame buffer capture has occurred. The
         * bytes will be in the order given by GL. To use this in a swing
         * context, you may have to reorder the bytes to account for the
         * different coordinate system.
         *
         * @param width Width of the captured image.
         * @param height Height of the captured image.
         * @param screenCapture The captured image as GL.GL_RGB.
         */
        void handleFrameBufferCaptured(int width, int height, byte[] screenCapture);
    }

    /** Interface for a frame buffer capture provider. */
    public interface FrameBufferCaptureProvider
    {
        /**
         * Discontinue receiving frame buffer captures.
         *
         * @param listener Listener for the captured frame.
         */
        void cancelCapture(FrameBufferCaptureListener listener);

        /**
         * Attempt to capture a frame at each interval until cancelled.
         *
         * @param listener Listener for the captured frames.
         * @param interval the interval at which to capture frames.
         */
        void captureScheduled(FrameBufferCaptureListener listener, int interval);

        /**
         * Capture a single frame and discontinue listening.
         *
         * @param listener Listener for the captured frame.
         */
        void captureSingleFrame(FrameBufferCaptureListener listener);

        /**
         * Capture a frame each time the frame buffer is rendered until
         * cancelled.
         *
         * @param listener Listener for the captured frames.
         */
        void captureStream(FrameBufferCaptureListener listener);
    }
}
