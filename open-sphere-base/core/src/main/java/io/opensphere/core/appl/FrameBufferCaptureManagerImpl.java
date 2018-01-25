package io.opensphere.core.appl;

import io.opensphere.core.FrameBufferCaptureManager;
import io.opensphere.core.util.Utilities;

/** Implementation of the {@link FrameBufferCaptureManager} interface. */
public class FrameBufferCaptureManagerImpl implements FrameBufferCaptureManager
{
    /**
     * Provider for capture requests. This is initialized with an empty
     * implementation to satisfy the contract that this is never {@code null}.
     */
    private FrameBufferCaptureProvider myCaptureProvider = new FrameBufferCaptureProvider()
    {
        @Override
        public void cancelCapture(FrameBufferCaptureListener listener)
        {
        }

        @Override
        public void captureScheduled(FrameBufferCaptureListener listener, int interval)
        {
        }

        @Override
        public void captureSingleFrame(FrameBufferCaptureListener listener)
        {
        }

        @Override
        public void captureStream(FrameBufferCaptureListener listener)
        {
        }
    };

    @Override
    public FrameBufferCaptureProvider getCaptureProvider()
    {
        return myCaptureProvider;
    }

    @Override
    public void setCaptureProvider(FrameBufferCaptureProvider provider)
    {
        Utilities.checkNull(provider, "provider");
        myCaptureProvider = provider;
    }
}
