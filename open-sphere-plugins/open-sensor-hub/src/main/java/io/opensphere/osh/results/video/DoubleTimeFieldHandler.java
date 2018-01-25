package io.opensphere.osh.results.video;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import io.opensphere.core.util.Constants;

/**
 * Reads a double time stamp.
 */
public class DoubleTimeFieldHandler implements VideoFieldHandler
{
    @Override
    public boolean readField(InputStream stream, VideoData videoData) throws IOException
    {
        boolean eof = false;
        DataInputStream dataStream = new DataInputStream(stream);
        try
        {
            long time = (long)(dataStream.readDouble() * Constants.MILLI_PER_UNIT);
            if (videoData.getTime() == 0)
            {
                videoData.setTime(time);
            }
        }
        catch (EOFException e)
        {
            eof = true;
        }
        return eof;
    }
}
