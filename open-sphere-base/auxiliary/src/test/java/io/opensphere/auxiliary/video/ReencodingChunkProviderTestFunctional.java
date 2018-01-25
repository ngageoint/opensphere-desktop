package io.opensphere.auxiliary.video;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.xuggle.xuggler.IContainer;

import gnu.trove.list.TLongList;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.ListOfBytesOutputStream;
import io.opensphere.core.video.ChunkException;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoTestUtil;

/**
 * Tests the {@link ReencodingChunkProvider}.
 */
public class ReencodingChunkProviderTestFunctional
{
    /**
     * The actual number of video chunks.
     */
    private int myNumberOfChunks;

    /**
     * The actual number of metadata chunks.
     */
    private int myNumberOfMetadatas;

    /**
     * Tests providing video chunks and metadata from a test klv file.
     *
     * @throws ChunkException Bad chunk.
     * @throws FileNotFoundException Bad file.
     */
    @Test
    public void testProvideChunksWithMetadata() throws ChunkException, FileNotFoundException
    {
        myNumberOfChunks = 0;
        myNumberOfMetadatas = 0;

        CancellableInputStream fileStream = VideoTestUtil.getFileStream(System.getProperty("klvFile1", VideoTestUtil.KLV_FILE_1));

        XugglerNativeUtilities.explodeXugglerNatives();

        IContainer inputContainer;
        inputContainer = IContainer.make();

        String feedName = "testFeed";
        if (inputContainer.open(fileStream, null) < 0)
        {
            throw new ChunkException("Could not open video stream " + feedName);
        }

        EasyMockSupport support = new EasyMockSupport();

        long start = System.currentTimeMillis();

        VideoChunkConsumer consumer = createConsumer(support);
        VideoContentHandler<ByteBuffer> contentHandler = createContentHandler(support);

        support.replayAll();

        ReencodingChunkProvider chunkProvider = new ReencodingChunkProvider(fileStream, inputContainer, start, 5000);
        chunkProvider.provideChunks(consumer, contentHandler);

        assertEquals(166, myNumberOfChunks);
        assertEquals(299, myNumberOfMetadatas);

        support.verifyAll();
    }

    /**
     * Tests providing video chunks and metadata from a test klv file.
     *
     * @throws ChunkException Bad chunk.
     * @throws FileNotFoundException Bad file.
     */
    @Test
    public void testProvideChunksWithMetadata2() throws ChunkException, FileNotFoundException
    {
        myNumberOfChunks = 0;
        myNumberOfMetadatas = 0;

        CancellableInputStream fileStream = VideoTestUtil.getFileStream(System.getProperty("klvFile2", VideoTestUtil.KLV_FILE_2));

        XugglerNativeUtilities.explodeXugglerNatives();

        IContainer inputContainer;
        inputContainer = IContainer.make();

        String feedName = "testFeed";
        if (inputContainer.open(fileStream, null) < 0)
        {
            throw new ChunkException("Could not open video stream " + feedName);
        }

        EasyMockSupport support = new EasyMockSupport();

        long start = System.currentTimeMillis();

        VideoChunkConsumer consumer = createConsumer(support);
        VideoContentHandler<ByteBuffer> contentHandler = createContentHandler(support);

        support.replayAll();

        ReencodingChunkProvider chunkProvider = new ReencodingChunkProvider(fileStream, inputContainer, start, 5000);
        chunkProvider.provideChunks(consumer, contentHandler);

        assertEquals(517, myNumberOfChunks);
        assertEquals(901, myNumberOfMetadatas);

        support.verifyAll();
    }

    /**
     * Adds one to myNumberOfMetadata.
     *
     * @return Null.
     */
    private Void contentHandlerAnswer()
    {
        ByteBuffer buffer = (ByteBuffer)EasyMock.getCurrentArguments()[0];
        if (buffer.hasRemaining())
        {
            myNumberOfMetadatas++;
        }

        return null;
    }

    /**
     * Creates an easy mock {@link VideoChunkConsumer}.
     *
     * @param support Used to create the mock.
     * @return The video chunk consumer that just ups myNumberOfChunks.
     */
    private VideoChunkConsumer createConsumer(EasyMockSupport support)
    {
        VideoChunkConsumer videoConsumer = support.createMock(VideoChunkConsumer.class);

        videoConsumer.consumeVideoChunk(EasyMock.anyLong(), EasyMock.anyLong(), EasyMock.isA(ListOfBytesOutputStream.class),
                EasyMock.isA(TLongList.class));
        EasyMock.expectLastCall().andAnswer(this::videoChunkAnswer).atLeastOnce();

        videoConsumer.consumeLastChunk(EasyMock.anyLong(), EasyMock.anyLong(), EasyMock.anyLong(),
                EasyMock.isA(ListOfBytesOutputStream.class), EasyMock.isA(TLongList.class));
        EasyMock.expectLastCall().andAnswer(this::videoChunkAnswer).atLeastOnce();

        return videoConsumer;
    }

    /**
     * Creates an easy mocked {@link VideoContentHandler} that ups
     * myNumberOfMetadatas.
     *
     * @param support Used to create the mock.
     * @return The mocked metadata handler.
     */
    @SuppressWarnings("unchecked")
    private VideoContentHandler<ByteBuffer> createContentHandler(EasyMockSupport support)
    {
        VideoContentHandler<ByteBuffer> handler = support.createMock(VideoContentHandler.class);

        handler.handleContent(EasyMock.isA(ByteBuffer.class), EasyMock.anyLong());
        EasyMock.expectLastCall().andAnswer(this::contentHandlerAnswer).atLeastOnce();

        return handler;
    }

    /**
     * Adds one to myNumberOfChunks.
     *
     * @return Null.
     */
    private Void videoChunkAnswer()
    {
        myNumberOfChunks++;
        return null;
    }
}
