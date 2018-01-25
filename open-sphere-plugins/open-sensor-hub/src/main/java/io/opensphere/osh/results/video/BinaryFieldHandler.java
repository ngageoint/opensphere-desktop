package io.opensphere.osh.results.video;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads binary data.
 */
public class BinaryFieldHandler implements VideoFieldHandler
{
    /** The output stream. */
    private final ByteArrayOutputStream myOutputStream = new ByteArrayOutputStream(1 << 15);

    @Override
    public boolean readField(InputStream stream, VideoData videoData) throws IOException
    {
        boolean eof = false;
        DataInputStream dataStream = new DataInputStream(stream);
        try
        {
            int byteCount = dataStream.readInt();
            int bytesRead = ByteUtilities.readNBytes(stream, myOutputStream, byteCount);
            videoData.setData(myOutputStream.toByteArray());
            myOutputStream.reset();
            eof = bytesRead < byteCount;
        }
        catch (EOFException e)
        {
            eof = true;
        }
        return eof;
    }
}
