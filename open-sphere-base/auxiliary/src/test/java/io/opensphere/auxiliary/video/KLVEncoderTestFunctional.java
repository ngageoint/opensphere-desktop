package io.opensphere.auxiliary.video;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoDecoderException;
import io.opensphere.core.video.VideoEncoderException;
import io.opensphere.core.video.VideoTestUtil;

/**
 * Tests the {@link KLVEncoder} class.
 */
@SuppressWarnings("boxing")
public class KLVEncoderTestFunctional
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(KLVEncoderTestFunctional.class);

    /**
     * Tests the klv encoder with video file 0.
     *
     * @throws VideoDecoderException Bad decoder.
     * @throws IOException Bad files.
     */
    @Test
    public void testFile0() throws VideoDecoderException, IOException
    {
        String filePath = System.getProperty("videoFile0", "X:\\rtmp\\badvideos\\2feedswithchat.flv");
        runTest(filePath, New.set(9642, 9642));
    }

    /**
     * Tests the klv encoder with video file 1.
     *
     * @throws VideoDecoderException Bad decoder.
     * @throws IOException Bad files.
     */
    @Test
    public void testFile1() throws VideoDecoderException, IOException
    {
        String filePath = System.getProperty("videoFile1", VideoTestUtil.VIDEO_FILE_1);
        runTest(filePath, New.set(8323, 8327, 8316));
    }

    /**
     * Tests the klv encoder with video file 1.
     *
     * @throws VideoDecoderException Bad decoder.
     * @throws IOException Bad files.
     */
    @Test
    public void testFile2() throws VideoDecoderException, IOException
    {
        String filePath = System.getProperty("videoFile2", VideoTestUtil.VIDEO_FILE_2);
        runTest(filePath, New.set(8340, 8344, 8333));
    }

    /**
     * Tests the klv encoder with video file 1.
     *
     * @throws VideoDecoderException Bad decoder.
     * @throws IOException Bad files.
     */
    @Test
    public void testFile3() throws VideoDecoderException, IOException
    {
        String filePath = System.getProperty("videoFile3", VideoTestUtil.VIDEO_FILE_3);
        runTest(filePath, New.set(8294, 8298, 8287));
    }

    /**
     * Tests the klv encoder with video file 1.
     *
     * @throws VideoDecoderException Bad decoder.
     * @throws IOException Bad files.
     */
    @Test
    public void testFile4() throws VideoDecoderException, IOException
    {
        String filePath = System.getProperty("videoFile4", VideoTestUtil.VIDEO_FILE_4);
        runTest(filePath, New.set(8949, 8949));
    }

    /**
     * Tests the klv encoder with video file 1.
     *
     * @throws VideoDecoderException Bad decoder.
     * @throws IOException Bad files.
     */
    @Test
    public void testFile5() throws VideoDecoderException, IOException
    {
        String filePath = System.getProperty("videoFile5", VideoTestUtil.VIDEO_FILE_5);
        runTest(filePath, New.set(8963, 8963));
    }

    /**
     * Create an easy mocked video handler that handles images when verifying a
     * create klv file.
     *
     * @param support Used to create the mock.
     * @param imageTimes The list to add the image times to.
     * @return The mocked {@link VideoContentHandler}.
     */
    @SuppressWarnings("unchecked")
    private VideoContentHandler<ImageIOImage> createKlvVideoHandler(EasyMockSupport support, List<Long> imageTimes)
    {
        VideoContentHandler<ImageIOImage> handler = support.createMock(VideoContentHandler.class);

        handler.handleContent(EasyMock.isA(ImageIOImage.class), EasyMock.anyLong());
        EasyMock.expectLastCall().andAnswer(() -> handleKlvContent(imageTimes)).anyTimes();

        return handler;
    }

    /**
     * Creates an easy mocked metadata handler.
     *
     * @param support Used to create the mock.
     * @param videoTimes The list of frame times decoded, current impl of
     *            {@link KLVEncoder} needs this to be empty at this time.
     * @param metadataTimes The list to add the metadata to.
     * @return The mocked {@link VideoContentHandler}.
     */
    @SuppressWarnings("unchecked")
    private VideoContentHandler<ByteBuffer> createMetadataHandler(EasyMockSupport support, List<Long> videoTimes,
            List<Long> metadataTimes)
    {
        VideoContentHandler<ByteBuffer> handler = support.createMock(VideoContentHandler.class);

        handler.handleContent(EasyMock.isA(ByteBuffer.class), EasyMock.anyLong());
        EasyMock.expectLastCall().andAnswer(() -> handleMetadata(videoTimes, metadataTimes)).anyTimes();

        return handler;
    }

    /**
     * Creates an easy mocked video handler that will encode decoded images into
     * a klv file.
     *
     * @param support Used to create the mock.
     * @param encoder The {@link KLVEncoder}.
     * @param imageTimes The list to add decoded image times to.
     * @param metadataTimes The list to add metadata added to the klv to.
     * @return The mocked {@link VideoContentHandler}.
     */
    @SuppressWarnings("unchecked")
    private VideoContentHandler<ImageIOImage> createVideoHandler(EasyMockSupport support, KLVEncoder encoder,
            List<Long> imageTimes, List<Long> metadataTimes)
    {
        VideoContentHandler<ImageIOImage> handler = support.createMock(VideoContentHandler.class);

        handler.handleContent(EasyMock.isA(ImageIOImage.class), EasyMock.anyLong());
        EasyMock.expectLastCall().andAnswer(() -> handleContent(encoder, imageTimes, metadataTimes)).anyTimes();

        return handler;
    }

    /**
     * The answer function for the easy mocked video handler that encodes klv.
     *
     * @param encoder The {@link KLVEncoder} to use.
     * @param imageTimes The list to add encoded image times to.
     * @param metadataTimes The list to add encoded metadata to.
     * @return Null.
     */
    private Void handleContent(KLVEncoder encoder, List<Long> imageTimes, List<Long> metadataTimes)
    {
        ImageIOImage image = (ImageIOImage)EasyMock.getCurrentArguments()[0];
        long ptsMs = (long)EasyMock.getCurrentArguments()[1];

        try
        {
            imageTimes.add(ptsMs);
            encoder.encodeVideo(image.getAWTImage(), ptsMs);
            if (imageTimes.size() % 30 == 0)
            {
                long metadata = imageTimes.size() / 30;
                metadataTimes.add(metadata);
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.putLong(metadata);

                encoder.encodeMetadata(buffer, ptsMs);
            }
        }
        catch (VideoEncoderException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        finally
        {
            image.dispose();
        }

        return null;
    }

    /**
     * The answer for the mocked klv video handler.
     *
     * @param imageTimes The list to add image times to.
     * @return Null.
     */
    private Void handleKlvContent(List<Long> imageTimes)
    {
        ImageIOImage image = (ImageIOImage)EasyMock.getCurrentArguments()[0];
        try
        {
            long ptsMs = (long)EasyMock.getCurrentArguments()[1];

            if (image.getAWTImage() != null)
            {
                imageTimes.add(ptsMs);
            }
        }
        finally
        {
            image.dispose();
        }

        return null;
    }

    /**
     * The answer for the mocked metadata handler.
     *
     * @param videoTimes The list of frame times decoded, current impl of
     *            {@link KLVEncoder} needs this to be empty at this time.
     * @param metadataTimes The list to add the metadata to.
     * @return Null.
     */
    private Void handleMetadata(List<Long> videoTimes, List<Long> metadataTimes)
    {
        ByteBuffer buffer = (ByteBuffer)EasyMock.getCurrentArguments()[0];

        if (videoTimes.isEmpty())
        {
            metadataTimes.add(buffer.getLong());
        }

        return null;
    }

    /**
     * Runs the KLVEncoder test with the specified video file as input.
     *
     * @param inputFilePath The path to the video file.
     * @param validImageCounts The set of valid image counts for the test file
     *            these change depending on what system the test is run on.
     * @throws VideoDecoderException Bad decoder.
     * @throws IOException Bad IO.
     */
    private void runTest(String inputFilePath, Set<Integer> validImageCounts) throws VideoDecoderException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        File testOutFile = File.createTempFile("test", ".ts");
        LOGGER.info("Writing klv file " + testOutFile);
        FileOutputStream outputStream = new FileOutputStream(testOutFile);
        KLVEncoder encoder = new KLVEncoder();
        encoder.init(outputStream);
        List<Long> imageTimes = New.list();
        List<Long> metadataTimes = New.list();

        VideoContentHandler<ImageIOImage> handler = createVideoHandler(support, encoder, imageTimes, metadataTimes);

        List<Long> actualImageTimes = New.list();
        VideoContentHandler<ImageIOImage> klvVideoHandler = createKlvVideoHandler(support, actualImageTimes);

        List<Long> actualMetadata = New.list();
        VideoContentHandler<ByteBuffer> metadataHandler = createMetadataHandler(support, actualImageTimes, actualMetadata);

        support.replayAll();

        CancellableInputStream stream = VideoTestUtil.getFileStream(inputFilePath);
        try (XugglerVideoDecoder decoder = new XugglerVideoDecoder())
        {
            decoder.setInputStream(stream, null);
            decoder.registerVideoContentHandler(handler, null);
            decoder.decode();
            encoder.close();
        }

        stream = new CancellableInputStream(new FileInputStream(testOutFile), null);
        try (XugglerVideoDecoder decoder = new XugglerVideoDecoder())
        {
            decoder.setInputStream(stream, null);
            decoder.registerNonVideoContentHandler(metadataHandler);
            decoder.registerVideoContentHandler(klvVideoHandler, null);
            decoder.decode();
        }

        int imageCount = actualImageTimes.size();
        assertTrue("Unexpected Image count " + imageCount, validImageCounts.contains(imageCount));

        assertEquals(metadataTimes.size(), actualMetadata.size());
        for (int i = 0; i < metadataTimes.size(); i++)
        {
            assertEquals(metadataTimes.get(i), actualMetadata.get(i));
        }

        support.verifyAll();
    }
}
