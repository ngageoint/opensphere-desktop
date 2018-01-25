package io.opensphere.auxiliary.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import io.opensphere.core.video.VideoEncoder;
import io.opensphere.core.video.VideoEncoderException;

/**
 * A video encoder that uses Xuggler.
 */
@ThreadSafe
public class XugglerVideoEncoder implements VideoEncoder
{
    /** The writer which produces the video file. */
    private IMediaWriter myMediaWriter;

    /** The time at which recording was started. */
    private long myRecordStartTimeMillis;

    static
    {
        XugglerNativeUtilities.explodeXugglerNatives();
    }

    @Override
    public synchronized void close()
    {
        if (myMediaWriter != null && myMediaWriter.isOpen())
        {
            myMediaWriter.close();
        }
        myMediaWriter = null;
    }

    @Override
    public synchronized void encode(BufferedImage frame)
    {
        myMediaWriter.encodeVideo(0, frame, System.currentTimeMillis() - myRecordStartTimeMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void encode(BufferedImage frame, long timeStampMS)
    {
        myMediaWriter.encodeVideo(0, frame, timeStampMS, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized boolean isOpen()
    {
        return myMediaWriter != null && myMediaWriter.isOpen();
    }

    @Override
    public synchronized void open(File output, int width, int height) throws VideoEncoderException
    {
        if (myMediaWriter != null)
        {
            throw new IllegalStateException("Encoder is already open.");
        }
        try
        {
            myMediaWriter = ToolFactory.makeWriter(output.getAbsolutePath());
            myMediaWriter.addVideoStream(0, 0, width, height);
        }
        catch (RuntimeException e)
        {
            throw new VideoEncoderException("Error opening encoder: " + e, e);
        }
        myRecordStartTimeMillis = System.currentTimeMillis();
    }

    /**
     * Open this encoder with a specific media writer.
     *
     * @param writer The writer to use when writing video.
     */
    public synchronized void open(IMediaWriter writer)
    {
        myMediaWriter = writer;
    }
}
