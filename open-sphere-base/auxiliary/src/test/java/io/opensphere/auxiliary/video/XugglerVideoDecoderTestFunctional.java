package io.opensphere.auxiliary.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoDecoderException;

/**
 * Functional test for {@link XugglerVideoDecoder} class.
 */
public class XugglerVideoDecoderTestFunctional
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(XugglerVideoDecoderTestFunctional.class);

    /**
     * The path to the test file on a linux box.
     */
    private static final String ourLinuxPath = "/data/rtmp/";

    /**
     * The test video file.
     */
    private static final String ourTestVideoFile = "badDataVideo";

    /**
     * The path to the test file on a windows box.
     */
    private static final String ourWinPath = "x:\\rtmp\\";

    /**
     * Tests a video file with a bad frame.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testBadFrames() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        @SuppressWarnings("unchecked")
        VideoContentHandler<ImageIOImage> handler = support.createMock(VideoContentHandler.class);
        handler.handleContent(EasyMock.isA(ImageIOImage.class), EasyMock.anyLong());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                ImageIOImage img = (ImageIOImage)EasyMock.getCurrentArguments()[0];
                img.dispose();
                return null;
            }
        }).times(4529, 4533);

        File file = new File(System.getProperty("RtmpTestFilesDirWin", ourWinPath) + ourTestVideoFile);
        if (!file.exists())
        {
            file = new File(System.getProperty("RtmpTestFilesDirLinux", ourLinuxPath) + ourTestVideoFile);
        }

        FileInputStream stream = new FileInputStream(file);

        support.replayAll();

        try (XugglerVideoDecoder decoder = new XugglerVideoDecoder())
        {
            decoder.registerVideoContentHandler(handler, (Vector2i)null);

            try
            {
                decoder.setInputStream(new CancellableInputStream(stream, (Runnable)null), TimeInstant.get());
                decoder.decode();
            }
            catch (VideoDecoderException e)
            {
                LOGGER.info(e, e);
            }
        }

        support.verifyAll();
    }
}
